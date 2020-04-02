package com.certificationschedular.demo;

import java.util.List;

import com.certificationschedular.demo.model.DeviceDetail;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;

public class ConsoleItemWriter<T> implements ItemWriter<T> {
    public static final int COUNTER_FOR_AVARAGE = 10;
    private StepExecution stepExecution;

    @Override
    public void write(List<? extends T> items) throws Exception {
        long responseTime = 0;
        for (T item : items) {
            DeviceDetail itemnew = (DeviceDetail) item;
            System.out.println(itemnew.toString());
            responseTime = itemnew.getValidationEndTime() - itemnew.getValidationStartTime();
        }

        statisticalCalculation(responseTime);

    }

    private void statisticalCalculation(long responseTime) {
        ExecutionContext stepContext = this.stepExecution.getExecutionContext();
        int count = stepContext.containsKey("deviceCounter") ? stepContext.getInt("deviceCounter") : 0;
        long deviceCumulativeResponseTime = stepContext.containsKey("deviceCumulativeResponseTime") ? stepContext.getLong("deviceCumulativeResponseTime") : 0;

        if (count == (COUNTER_FOR_AVARAGE-1)) {
            System.out.println("========================================================================");
            System.out.println("avarageResponseTimePer10min : " + Math.round(deviceCumulativeResponseTime / COUNTER_FOR_AVARAGE));
            System.out.println("========================================================================");
            count = 0;
            deviceCumulativeResponseTime = 0;
        }
        this.stepExecution.getExecutionContext().put("deviceCounter", count + 1);
        this.stepExecution.getExecutionContext().put("deviceCumulativeResponseTime", deviceCumulativeResponseTime + responseTime);
    }

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        if (this.stepExecution == null)
            this.stepExecution = stepExecution;
    }
}