package com.github.sociallabel;

import java.io.File;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class WebAppInitializer implements WebApplicationInitializer {

	private int maxUploadSizeInMb = 10 * 1024 * 1024; // 10 MB

	@Override
	public void onStartup(ServletContext container) {
		AnnotationConfigWebApplicationContext mvcContext = new AnnotationConfigWebApplicationContext();
		mvcContext.register(AppConfig.class);
		mvcContext.register(WebConfig.class);
		mvcContext.setServletContext(container);
		mvcContext.refresh();

		container.addListener(new ContextLoaderListener(mvcContext));		
				
		DispatcherServlet dispatcherServlet = new DispatcherServlet(mvcContext);
		OpenEntityManagerInViewFilter filter = new OpenEntityManagerInViewFilter();
		filter.setServletContext(container);
		
		Dynamic filterRegistration = container.addFilter("openEntityManagerInViewFilter", filter);
		filterRegistration.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false, "dispatcher");
		
		CharacterEncodingFilter cfilter = new CharacterEncodingFilter();
		cfilter.setEncoding("UTF-8");
		cfilter.setForceEncoding(true);
		cfilter.setServletContext(container);
		
		filterRegistration = container.addFilter("characterEncodingFilter", cfilter);
		filterRegistration.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false, "dispatcher");
		
		ServletRegistration.Dynamic registration = container.addServlet("dispatcher", dispatcherServlet);
		File tempdir = (File) container.getAttribute("javax.servlet.context.tempdir");
		registration.setMultipartConfig(new MultipartConfigElement(tempdir.getAbsolutePath(), maxUploadSizeInMb, maxUploadSizeInMb * 2, maxUploadSizeInMb));
		registration.setLoadOnStartup(1);
		registration.addMapping("/");		
	}

}
