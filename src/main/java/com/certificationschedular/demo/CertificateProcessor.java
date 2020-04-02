package com.certificationschedular.demo;

import com.certificationschedular.demo.model.DeviceDetail;
import com.certificationschedular.demo.util.SSLUtil;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sun.security.x509.X509CertImpl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service
public class CertificateProcessor implements ItemProcessor<DeviceDetail, DeviceDetail> {

    private static final HostnameVerifier PROMISCUOUS_VERIFIER = (s, sslSession) -> true;
    @Autowired
    private RestTemplate restTemplate;

    private void restTemplateBuilder(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public DeviceDetail process(final DeviceDetail deviceDetail) throws Exception {

        if (deviceDetail.getDeviceId() == null || "".equals(deviceDetail.getDeviceId())) {
            throw new Exception("DeviceId is null or empty!!");
        }

        if (deviceDetail.getC1() == null || "".equals(deviceDetail.getC1())) {
            throw new Exception("C1 is null or empty!!");
        }

        if (deviceDetail.getC2() == null || "".equals(deviceDetail.getC2())) {
            throw new Exception("C2 is null or empty!!");
        }

        if (deviceDetail.getC3() == null || "".equals(deviceDetail.getC3())) {
            throw new Exception("C3 is null or empty!!");
        }

        return getDeviceData(deviceDetail);
    }


    private DeviceDetail getDeviceData(DeviceDetail deviceDetail) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("https://devices.cgbe.trustonic.com/");
        sb.append(deviceDetail.getC1());
        sb.append("/");
        sb.append(deviceDetail.getC2());
        sb.append("/");
        sb.append(deviceDetail.getC3());
        sb.append("/");
        sb.append(deviceDetail.getDeviceId());
        sb.append(".pem");

        final String uri = sb.toString();

        return processPem(uri, deviceDetail.getDeviceId(), deviceDetail);
    }

    private DeviceDetail processPem(String uri, String deviceId, DeviceDetail deviceDetail) throws Exception {
        DeviceDetail deviceDetailWithCert = deviceDetail;
        deviceDetailWithCert.setValidationStartTime(ZonedDateTime.now().toInstant().toEpochMilli());

        SSLUtil.turnOffSslChecking();

        if (restTemplate == null) restTemplateBuilder(new RestTemplateBuilder());

        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                if (connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection).setHostnameVerifier(PROMISCUOUS_VERIFIER);
                }
                super.prepareConnection(connection, httpMethod);
            }
        });

        String certificateChainString = restTemplate.getForObject(uri, String.class);

        SSLUtil.turnOnSslChecking();

        deviceDetailWithCert.setCertificationValidationStatus(loadCertificate(certificateChainString, deviceId));
        deviceDetailWithCert.setValidationEndTime(ZonedDateTime.now().toInstant().toEpochMilli());

        return deviceDetailWithCert;

    }

    /***
     * Used for to process certificate chain
     * @param certificateChainString
     * @return
     */
    private boolean loadCertificate(String certificateChainString, String deviceId) {
        try {
            List<X509CertImpl> certificateChain = new ArrayList<>();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection c = cf.generateCertificates(
                    new ByteArrayInputStream(
                            certificateChainString.getBytes()
                    )
            );

            Iterator i = c.iterator();
            String previousChainIssuerDeviceId = "";
            String currentChainDeviceId;

            boolean deviceIdExistInCertList = false;
            while (i.hasNext()) {
                X509CertImpl x509Certificate = (X509CertImpl) i.next();
                certificateChain.add(x509Certificate);

                currentChainDeviceId = x509Certificate.getSubjectDN().toString().split(",")[0].split("=")[1];

                if (!deviceIdExistInCertList && deviceId.equals(currentChainDeviceId)) {
                    deviceIdExistInCertList = true;
                    continue;
                } else if (!deviceIdExistInCertList && !deviceId.equals(currentChainDeviceId)) {
                    return false;
                }

                if (!"".equals(previousChainIssuerDeviceId) && !currentChainDeviceId.equals(previousChainIssuerDeviceId)) {
                    return false;
                }
                previousChainIssuerDeviceId = x509Certificate.getIssuerDN().toString().split(",")[0].split("=")[1];
            }

            return true;
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}