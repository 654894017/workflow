package com.damon.workflow.spring;

import com.damon.workflow.conditionparser.IConditionParser;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.handler.IProcessStateHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

public class WorkflowBeanNameGenerator extends AnnotationBeanNameGenerator {
    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        Class beanClass = null;
        try {
            beanClass = Class.forName(definition.getBeanClassName());
        } catch (ClassNotFoundException e) {
            throw new ProcessException(e);
        }
        if (IProcessStateHandler.class.isAssignableFrom(beanClass) || IConditionParser.class.isAssignableFrom(beanClass)) {
            return definition.getBeanClassName();
        }
        return super.buildDefaultBeanName(definition);
    }

}