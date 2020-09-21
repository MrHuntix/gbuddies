package com.gbuddies.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;
import org.springframework.jmx.support.RegistrationPolicy;

@Configuration
public class JMXConfiguration {
    @Bean
    @Lazy
    public MBeanExporter mBeanExporter() {
        MBeanExporter exporter = new MBeanExporter();
        exporter.setAutodetect(true);
        exporter.setAssembler(metadataMBeanInfoAssembler());
        exporter.setRegistrationPolicy(RegistrationPolicy.REPLACE_EXISTING);
        exporter.setNamingStrategy(metadataNamingStrategy());
        return exporter;
    }

    @Bean
    public AnnotationJmxAttributeSource annotationJmxAttributeSource() {
        return new AnnotationJmxAttributeSource();
    }

    private MetadataMBeanInfoAssembler metadataMBeanInfoAssembler() {
        MetadataMBeanInfoAssembler metadataMBeanInfoAssembler = new MetadataMBeanInfoAssembler();
        metadataMBeanInfoAssembler.setAttributeSource(annotationJmxAttributeSource());
        return metadataMBeanInfoAssembler;
    }

    private MetadataNamingStrategy metadataNamingStrategy() {
        MetadataNamingStrategy metadataNamingStrategy = new MetadataNamingStrategy();
        metadataNamingStrategy.setAttributeSource(annotationJmxAttributeSource());
        return metadataNamingStrategy;
    }


}
