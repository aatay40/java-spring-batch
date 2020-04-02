package com.certificationschedular.demo;


import com.certificationschedular.demo.model.DeviceDetail;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job readCSVFilesJob() {
        return jobBuilderFactory
                .get("readCSVFilesJob")
                .incrementer(new RunIdIncrementer())
                .start(stepDeviceCertificationValidation())
                .build();
    }

    @Bean
    public Step stepDeviceCertificationValidation() {
        return stepBuilderFactory
                .get("stepDeviceCertificationValidation")
                .<DeviceDetail, DeviceDetail>chunk(1)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .listener(promotionListener())
                .build();
    }

    @Bean
    public FlatFileItemReader<DeviceDetail> reader() {
        FlatFileItemReader<DeviceDetail> itemReader = new FlatFileItemReader<DeviceDetail>();
        itemReader.setLineMapper(lineMapper());
        itemReader.setLinesToSkip(1);
        itemReader.setResource(new ClassPathResource("inputData.csv"));
        return itemReader;
    }

    @Bean
    public LineMapper<DeviceDetail> lineMapper() {
        DefaultLineMapper<DeviceDetail> lineMapper = new DefaultLineMapper<DeviceDetail>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames(new String[]{"device_id", "c1", "c2", "c3"});
        lineTokenizer.setIncludedFields(new int[]{0, 1, 2, 3});
        BeanWrapperFieldSetMapper<DeviceDetail> fieldSetMapper = new BeanWrapperFieldSetMapper<DeviceDetail>();
        fieldSetMapper.setTargetType(DeviceDetail.class);
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    @Bean
    public ConsoleItemWriter<DeviceDetail> writer() {
        return new ConsoleItemWriter<DeviceDetail>();
    }

    @Bean
    public CertificateProcessor processor() {
        return new CertificateProcessor();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"deviceCounter", "deviceCumulativeResponseTime"});
        return listener;
    }
}