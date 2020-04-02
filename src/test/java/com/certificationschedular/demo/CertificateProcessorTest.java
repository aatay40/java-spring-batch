package com.certificationschedular.demo;

import com.certificationschedular.demo.model.DeviceDetail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CertificateProcessorTest {
    private static final HostnameVerifier PROMISCUOUS_VERIFIER = (s, sslSession) -> true;
    @Autowired
    CertificateProcessor certificateProcessor;
    @Mock
    RestTemplate restTemplate;

    private String sampleCertData = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDxzCCAi+gAwIBAgIIB2SgINGjZzEwDQYJKoZIhvcNAQEMBQAwVzETMBEGCgmS\n" +
            "JomT8ixkARkWA2NvbTEZMBcGCgmSJomT8ixkARkWCVRydXN0b25pYzEXMBUGCgmS\n" +
            "JomT8ixkARkWB0RldmljZXMxDDAKBgNVBAMTA0NBMTAeFw0xODAyMTYxMDE4MDVa\n" +
            "Fw0yMzAyMTUxMDE4MDVaMIGNMRMwEQYKCZImiZPyLGQBGRYDQ29tMRkwFwYKCZIm\n" +
            "iZPyLGQBGRYJVHJ1c3RvbmljMRcwFQYKCZImiZPyLGQBGRYHRGV2aWNlczEXMBUG\n" +
            "CgmSJomT8ixkARkWB1NBTVNVTkcxKTAnBgNVBAMMIDAyMDAwMDAwMTAwMDg5ZTgx\n" +
            "YzQ1OTRmNzc2MDMwMDAwMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE9czmqdyL\n" +
            "98kuWgdxyP/Su1cD2Z5TRFEMXCDgNVBifiyWsfaHnzG0StCEPCcjlN0KZ/qPKkUO\n" +
            "ZHQueuDOutEsEaOBqjCBpzAsBgorBgEEAYL9DAEDBB4wHAQQAgAAABAAiegcRZT3\n" +
            "dgMAABMIYjEvMmIvNzkwEwYKKwYBBAGC/QwBBAQFMQMCAQEwHQYDVR0OBBYEFESx\n" +
            "SII1Rh7L+H5OX/wH9O7oIfovMB8GA1UdIwQYMBaAFGr0UpDyX2GC7jqFcYjZPHPx\n" +
            "jVDDMA4GA1UdDwEB/wQEAwIChDASBgNVHRMBAf8ECDAGAQH/AgEAMA0GCSqGSIb3\n" +
            "DQEBDAUAA4IBgQAjr0dNXLxhO3NOHtbWMsbP9dKsg17kNXpffsA7swAnRWko0Vun\n" +
            "oMvWxI0xrj6PVRqZPwhJTmoewHfdJqmet8vmx5FL+y8IKxnHotKwcNVEsuLdKVzR\n" +
            "PkIvscrFwHeGBoZOcTJoT87GX3S7gib25E5wF2NHkwjWqueinLbpcvra/9Pz6XqH\n" +
            "iB1nqC0Gm6fLY4BRsohVneDw05+A9lbsgAa9hZZbHjQ4wtQTqUWc1AV7EpNEMRl8\n" +
            "CEoPbTxYphqJLC96z5qdyqz8AL31rRjt4FbtsYiVzx/ehI9hw3/h2vpGnREz/3OZ\n" +
            "RTckjn2PTcyU0YDj9Ov9kw+LHEAW0tObJgY0BdyNREm3d5+EIkit5QvkRKWFzAnl\n" +
            "g4UibaYH6bHiethAXnPTpvJ5YG2iSPYCPAzpJjeKdp4AqZFQEnZqHi5NsRWdEbfd\n" +
            "QUkeMvR+ZNWGAXCwA1XeSgCqLjDjLE730HxskZtkjdqagHzYWTC5968p17TwoT/p\n" +
            "/taUHtMzttnMeeE=\n" +
            "-----END CERTIFICATE-----\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIGFDCCBHygAwIBAgIEV7X1FTANBgkqhkiG9w0BAQwFADBeMRMwEQYKCZImiZPy\n" +
            "LGQBGRYDY29tMRkwFwYKCZImiZPyLGQBGRYJVHJ1c3RvbmljMRcwFQYKCZImiZPy\n" +
            "LGQBGRYHRGV2aWNlczETMBEGA1UEAxMKSXNzdWluZyBDQTAeFw0xNjEwMDcxNDM5\n" +
            "MjlaFw0zMDEwMDcxNTA5MjlaMFcxEzARBgoJkiaJk/IsZAEZFgNjb20xGTAXBgoJ\n" +
            "kiaJk/IsZAEZFglUcnVzdG9uaWMxFzAVBgoJkiaJk/IsZAEZFgdEZXZpY2VzMQww\n" +
            "CgYDVQQDEwNDQTEwggGiMA0GCSqGSIb3DQEBAQUAA4IBjwAwggGKAoIBgQCd5y+h\n" +
            "fwGPZMCP9d7D60Cmky+4kroiPYRF9YwxzMQ20l84+UL4lzLflUEvI+3bDRINCytN\n" +
            "7S1oYWbtwFmPlDlmtBFKUGOSyPfFOnzPGiX/eCQ0EUQHHkwbPUS8+6IMFn0rYZK1\n" +
            "WUCUPLNXnVgSUV4+jri4pb0m3K733C1yjp12U7OgwbOjrh1GtK+C2IHTlY3vtA7S\n" +
            "A1R7KoTLb89qHUfp/GkmAPMdzCBKitMgCcGWInYJ1/xLBjflOCOsMdzTa+HcBvA1\n" +
            "r+lMBjAPfpgIn0lPaNSIkEkees2Qk/lYxZPEVv2XZcNBQc7VsHDrjuZRVaophUDn\n" +
            "pdsIYau0HlMUQKtMBMvnsWQtl/HXbXwUAio5hPkM6+7ZHX3X5MPdm/gQaOCzmjkU\n" +
            "57RlQ09fNo5Q2u9C3oJVH07Tt9c2qXbC1EWyIJjDYj0eNcgEHMCtWlvQPBSKbO3h\n" +
            "HrPRKJ+zPoPUqfFmxZp1MXPTpMy5fccvMlRFb7snA0kE1NsMr8BmpW+1+80CAwEA\n" +
            "AaOCAd8wggHbMA4GA1UdDwEB/wQEAwIBBjASBgNVHRMBAf8ECDAGAQH/AgEBMB0G\n" +
            "A1UdIAEB/wQTMBEwDwYNYIZIAYb6a4FIAwopAzBrBggrBgEFBQcBAQRfMF0wWwYI\n" +
            "KwYBBQUHMAKGT2h0dHA6Ly9jcmx0cnVzdG9uaWMubWFuYWdlZC5lbnRydXN0LmNv\n" +
            "bS9BSUEvQ2VydHNJc3N1ZWRUb1RydXN0b25pY0lzc3VpbmdDQS5wN2MwHQYDVR0O\n" +
            "BBYEFGr0UpDyX2GC7jqFcYjZPHPxjVDDMIHNBgNVHR8EgcUwgcIwSaBHoEWGQ2h0\n" +
            "dHA6Ly9jcmx0cnVzdG9uaWMubWFuYWdlZC5lbnRydXN0LmNvbS9DUkxzL3RydXN0\n" +
            "b25pY2lzc3VpbmdDQS5jcmwwdaBzoHGkbzBtMRMwEQYKCZImiZPyLGQBGRYDY29t\n" +
            "MRkwFwYKCZImiZPyLGQBGRYJVHJ1c3RvbmljMRcwFQYKCZImiZPyLGQBGRYHRGV2\n" +
            "aWNlczETMBEGA1UEAxMKSXNzdWluZyBDQTENMAsGA1UEAxMEQ1JMMTAfBgNVHSME\n" +
            "GDAWgBRdrrcOpU5mTOX113vYkrMA2ytO9zAZBgkqhkiG9n0HQQAEDDAKGwRWOC4x\n" +
            "AwIAgTANBgkqhkiG9w0BAQwFAAOCAYEANc8B9WHC4/2k18sitASf7kG8rSyLAVNF\n" +
            "1gvg5zGbZoY1FRmObud60UjBbpplmXUtUcBLlrzct8C6oFz4DfvD7pTZLILXZ7P7\n" +
            "PHnclqZtqBeXvvUrkGOHswXRJq5S8tlfyVwTdxbRhZqfFenOaC1z10EMnzYrOaKP\n" +
            "BqqvQOHM3FKaErHrNYNG7uygkKzBMgRTp2MKlqL6+niR9Rvp0smuw8XbKRfFkZug\n" +
            "1nwErwv/p1VrfO2RVzkugMmkEICcI07DTZuxdX18jWNpd1JtUrlRuy3RmPX7hev2\n" +
            "nbgPTFHLw8acbcB7fbAvZO3YF7ugFKTymIHq9OSXZFmVm6/xV83pXhoHkltoHJhY\n" +
            "KPHrilNiMPBApkrzCLA+HZzMlEiUYlCOMcPPcrQ/zb3PNrSt3heiyA91Uil3CKU8\n" +
            "Tem3+dz4KbBLiZg12lJXINoNA7R2huOzM7Qx8vIEhqG8KkO2U6pZOBVwLSceEL3r\n" +
            "WJAe2D1NKpM+dBFDlBGtvbz2cJRmPsOE\n" +
            "-----END CERTIFICATE-----\n" +
            "\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIGIzCCBIugAwIBAgIEV7XMHTANBgkqhkiG9w0BAQwFADBbMRMwEQYKCZImiZPy\n" +
            "LGQBGRYDY29tMRkwFwYKCZImiZPyLGQBGRYJVHJ1c3RvbmljMRcwFQYKCZImiZPy\n" +
            "LGQBGRYHRGV2aWNlczEQMA4GA1UEAxMHUm9vdCBDQTAeFw0xNjA4MTgxNzMyMTRa\n" +
            "Fw0zMDEwMTgxODAyMTRaMF4xEzARBgoJkiaJk/IsZAEZFgNjb20xGTAXBgoJkiaJ\n" +
            "k/IsZAEZFglUcnVzdG9uaWMxFzAVBgoJkiaJk/IsZAEZFgdEZXZpY2VzMRMwEQYD\n" +
            "VQQDEwpJc3N1aW5nIENBMIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEA\n" +
            "0oIjYXEYMmHyTuAD3yVGL+hvx9+HgOLGBhu1CLUU5VkDqGKhzZmbPJMKD3j+GvI9\n" +
            "xQjNM+dJrojVSdXqMnjYE6XJ9fgg4GLev71bjWkG2F0MhNiEGBTLS2oBZOJfid/c\n" +
            "MHnUnJkr33NHO4KCafkVO8ygDkP6jsihFmjrarRqWRTfDgeX2MyHqsLiP4qFd4jH\n" +
            "kYT3bepCuTy5kjGiow/JQUA1NicCm2oePuC5ahlpNVtMYRuNcavfoTrcTYbTHZ1x\n" +
            "fMDoXMEbbl7kyk+uEBoJpU9rCaEaEhRdW24p0l8/rAa117K/jYsEQx8SW5w2jW6l\n" +
            "V2mOzQUT5vL7I/NUf9Lyde38CohREHzMbQ+7ObMS2s2okuvHDoIONqkLSKpD7aNh\n" +
            "m8Wa6ux7edVOOx0XkNOoqdToWxZXdg0V9Mb/uBF3YFqORE/NMwr6Io3gE0Ugbv1Z\n" +
            "uPzkAL8QC9stbeEIYuHI8fciTk+ZxlG/oUhYAJMXlxH4m7nbjX9AyUg83SNkkJG7\n" +
            "AgMBAAGjggHqMIIB5jAOBgNVHQ8BAf8EBAMCAQYwEgYDVR0TAQH/BAgwBgEB/wIB\n" +
            "AjBMBgNVHSAERTBDMA4GDGCGSAGG+muBSAMKKTAPBg1ghkgBhvprgUgDCikBMA8G\n" +
            "DWCGSAGG+muBSAMKKQIwDwYNYIZIAYb6a4FIAwopAzBoBggrBgEFBQcBAQRcMFow\n" +
            "WAYIKwYBBQUHMAKGTGh0dHA6Ly9jcmx0cnVzdG9uaWMubWFuYWdlZC5lbnRydXN0\n" +
            "LmNvbS9BSUEvQ2VydHNJc3N1ZWRUb1RydXN0b25pY1Jvb3RDQS5wN2MwgccGA1Ud\n" +
            "HwSBvzCBvDBGoESgQoZAaHR0cDovL2NybHRydXN0b25pYy5tYW5hZ2VkLmVudHJ1\n" +
            "c3QuY29tL0NSTHMvdHJ1c3Rvbmljcm9vdGNhLmNybDByoHCgbqRsMGoxEzARBgoJ\n" +
            "kiaJk/IsZAEZFgNjb20xGTAXBgoJkiaJk/IsZAEZFglUcnVzdG9uaWMxFzAVBgoJ\n" +
            "kiaJk/IsZAEZFgdEZXZpY2VzMRAwDgYDVQQDEwdSb290IENBMQ0wCwYDVQQDEwRD\n" +
            "UkwxMB8GA1UdIwQYMBaAFG0cLWr4praBfevDJ1kXFvXeavcnMB0GA1UdDgQWBBRd\n" +
            "rrcOpU5mTOX113vYkrMA2ytO9zANBgkqhkiG9w0BAQwFAAOCAYEAJffVLegGlFgI\n" +
            "pETJ5/WzUULUKWZUWszQv1tC9T3XtpT7oU+6oIhDLQVCWTeJKPLtZbALtYcuGXk9\n" +
            "3NWqk6dtB/7MfwGRHr2dJ78kZt1hG5L/vo4+vnCgtkrM5u7XI3HXLj03SMucCiz2\n" +
            "HHse7sPlu+95yIXmDs8FA2lRCUT73M3XhQaclnurWzxg03cBg1GQr+CJg9nQm1u0\n" +
            "9JwwtJLt8ICq9Ty5io74R/WzNZKLyFrFMewtir379uKwN73kNPEB+Ss43PYL/+ro\n" +
            "WX+4BE765IheeXrfdEoqo8T30Jtjn36CRp8eT17bgUoXuyN4PE8kVKP55FjD9QFO\n" +
            "Ro5BfgfhCUOQQVupLsxH7I5uOYPp+79fCXZgd61/wS1A3CZgywI+L+nXYfIx9qhy\n" +
            "wO1wQ1IMv2JgYic4jJZ1SGbOxmxc6yS6q0asgwJg8oGOc+KndwhcIwBTNQnwxKFv\n" +
            "Qfp9L2Tpk+u0ip/YWiaWRYN9aY9AvxhhpkHcRq578kLdn7XdXt/Z\n" +
            "-----END CERTIFICATE-----\n";

    @BeforeEach
    public void init() {
        certificateProcessor = new CertificateProcessor();
        restTemplate = Mockito.mock(RestTemplate.class);
    }

    @Test
    public void testProcess_exceptions_deviceId() {
        DeviceDetail deviceDetail = new DeviceDetail();

        Exception exception = assertThrows(Exception.class, () -> {
            certificateProcessor.process(deviceDetail);
        });

        String expectedMessage = "DeviceId is null or empty!!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testProcess_exceptions_c1() {
        DeviceDetail deviceDetail = new DeviceDetail();
        deviceDetail.setDeviceId("02000000100089e81c4594f776030000");

        Exception exception = assertThrows(Exception.class, () -> {
            certificateProcessor.process(deviceDetail);
        });

        String expectedMessage = "C1 is null or empty!!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testProcess_exceptions_c2() {
        DeviceDetail deviceDetail = new DeviceDetail();
        deviceDetail.setDeviceId("02000000100089e81c4594f776030000");
        deviceDetail.setC1("b1");

        Exception exception = assertThrows(Exception.class, () -> {
            certificateProcessor.process(deviceDetail);
        });

        String expectedMessage = "C2 is null or empty!!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testProcess_exceptions_c3() {
        DeviceDetail deviceDetail = new DeviceDetail();
        deviceDetail.setDeviceId("02000000100089e81c4594f776030000");
        deviceDetail.setC1("b1");
        deviceDetail.setC2("2b");

        Exception exception = assertThrows(Exception.class, () -> {
            certificateProcessor.process(deviceDetail);
        });

        String expectedMessage = "C3 is null or empty!!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testProcess_with_mock_data() {
        Mockito.when(
                restTemplate.getForObject(Matchers.anyString(), Matchers.eq(String.class), Matchers.anyMap()))
                .thenReturn(sampleCertData);

        try {
            DeviceDetail deviceDetail = new DeviceDetail();
            deviceDetail.setDeviceId("02000000100089e81c4594f776030000");
            deviceDetail.setC1("b1");
            deviceDetail.setC2("2b");
            deviceDetail.setC3("79");
            DeviceDetail response = certificateProcessor.process(deviceDetail);
            Assertions.assertEquals("02000000100089e81c4594f776030000", response.getDeviceId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcess_exceptions_with_wrong_data() {
        Mockito.when(
                restTemplate.getForObject(Matchers.anyString(), Matchers.eq(String.class), Matchers.anyMap()))
                .thenReturn(sampleCertData);

        try {
            DeviceDetail deviceDetail = new DeviceDetail();
            deviceDetail.setDeviceId("xxxxxwrong-device-idxxxxxx");
            deviceDetail.setC1("b1");
            deviceDetail.setC2("2b");
            deviceDetail.setC3("79");

            Exception exception = assertThrows(Exception.class, () -> {
                certificateProcessor.process(deviceDetail);
            });

            HttpStatus errorStatus = ((HttpClientErrorException.BadRequest) exception).getStatusCode();

            assertTrue(HttpStatus.BAD_REQUEST == errorStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}