package de.invesdwin.webproxy.geolocation;

import javax.annotation.concurrent.Immutable;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ ServerTest.class })
@Immutable
public class GeolocationTestSuite {

}
