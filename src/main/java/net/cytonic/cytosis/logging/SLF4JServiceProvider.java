/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementaion'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */
package net.cytonic.cytosis.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;

/**
 * An SLF4J compatible logger
 *
 * @see org.slf4j.spi.SLF4JServiceProvider
 */
public class SLF4JServiceProvider implements org.slf4j.spi.SLF4JServiceProvider {
    /**
     * default constructor
     */
    public SLF4JServiceProvider() {
        // do nothing
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return SLF4JCompatibilityLayer::new;
    }

    private final IMarkerFactory markerFactory = new BasicMarkerFactory();
    private final MDCAdapter mdcAdapter = new NOPMDCAdapter();

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0";
    }

    @Override
    public void initialize() {
    }
}