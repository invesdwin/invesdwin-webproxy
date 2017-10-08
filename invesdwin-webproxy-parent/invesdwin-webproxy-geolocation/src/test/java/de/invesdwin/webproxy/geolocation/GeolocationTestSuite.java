package de.invesdwin.webproxy.geolocation;

import javax.annotation.concurrent.Immutable;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectClasses({ ServerTest.class })
@Immutable
public class GeolocationTestSuite {

}
