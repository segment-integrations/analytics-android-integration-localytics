package com.segment.analytics.android.integrations.localytics;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import com.localytics.android.Localytics;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.ValueMap;
import com.segment.analytics.core.tests.BuildConfig;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.GroupPayloadBuilder;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.ScreenPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.segment.analytics.Analytics.LogLevel.VERBOSE;
import static com.segment.analytics.Utils.createTraits;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18, manifest = Config.NONE)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*" })
@PrepareForTest(Localytics.class)
public class LocalyticsTest {
  @Rule public PowerMockRule rule = new PowerMockRule();
  @Mock Analytics analytics;
  LocalyticsIntegration integration;

  @Before public void setUp() {
    initMocks(this);
    PowerMockito.mockStatic(Localytics.class);
    when(analytics.getApplication()).thenReturn(RuntimeEnvironment.application);
    when(analytics.logger("Localytics")).thenReturn(Logger.with(VERBOSE));

    integration = new LocalyticsIntegration(analytics, new ValueMap().putValue("appKey", "foo"));
    integration.customDimensions = new ValueMap();
    // Mock twice so we can initialize a mock in setup, and reset the mock for following tests.
    PowerMockito.mockStatic(Localytics.class);
  }

  @Test public void initialize() {
    LocalyticsIntegration integration = new LocalyticsIntegration(analytics,
        new ValueMap().putValue("appKey", "foo").putValue("setOrganizationScope", true)
            .putValue("dimensions", new ValueMap().putValue("foo", "bar")));

    verifyStatic();
    Localytics.integrate(RuntimeEnvironment.application, "foo");
    verifyStatic();
    Localytics.setLoggingEnabled(true);
    assertThat(integration.customDimensions).isEqualTo(Collections.singletonMap("foo", "bar"));
    verifyStatic();
    assertThat(integration.attributeScope).isEqualTo(Localytics.ProfileScope.ORGANIZATION);
  }

  @Test public void activityResume() {
    Activity activity = mock(Activity.class);
    Intent intent = mock(Intent.class);
    when(activity.getIntent()).thenReturn(intent);
    integration.onActivityResumed(activity);
    verifyStatic();
    Localytics.openSession();
    verifyStatic();
    Localytics.upload();
    verifyStatic();
    Localytics.handleTestMode(intent);
    verifyStatic();
  }

  @Test public void activityResumeCompat() {
    FragmentActivity activity = mock(FragmentActivity.class);
    integration.onActivityResumed(activity);
    verifyStatic();
    Localytics.openSession();
    verifyStatic();
    Localytics.upload();
    verifyStatic();
    Localytics.setInAppMessageDisplayActivity(activity);
    verifyStatic();
  }

  @Test public void activityPause() {
    Activity activity = mock(Activity.class);
    integration.onActivityPaused(activity);
    verifyStatic();
    Localytics.closeSession();
    verifyStatic();
    Localytics.upload();
  }

  @Test public void activityPauseCompat() {
    FragmentActivity activity = mock(FragmentActivity.class);
    integration.onActivityPaused(activity);
    verifyStatic();
    Localytics.dismissCurrentInAppMessage();
    verifyStatic();
    Localytics.clearInAppMessageDisplayActivity();
    verifyStatic();
    Localytics.closeSession();
    verifyStatic();
    Localytics.upload();
  }

  @Test public void identify() {
    integration.identify(new IdentifyPayloadBuilder().traits(createTraits("foo")).build());

    verifyStatic();
    Localytics.setCustomerId("foo");
    verifyStatic();
    Localytics.setProfileAttribute("userId", "foo", Localytics.ProfileScope.APPLICATION);
  }

  @Test public void identifyWithSpecialFields() {
    integration.identify(new IdentifyPayloadBuilder().traits(
        createTraits("foo").putEmail("baz").putName("bar").putFirstName("bar").putLastName("foo").putValue("custom", "qaz")).build());

    verifyStatic();
    Localytics.setCustomerId("foo");
    verifyStatic();
    Localytics.setIdentifier("email", "baz");
    verifyStatic();
    Localytics.setCustomerEmail("baz");
    verifyStatic();
    Localytics.setCustomerFirstName("bar");
    verifyStatic();
    Localytics.setCustomerLastName("foo");
    verifyStatic();
    Localytics.setCustomerFullName("bar");
    verifyStatic();
    Localytics.setIdentifier("customer_name", "bar");
    verifyStatic();
    Localytics.setProfileAttribute("userId", "foo", Localytics.ProfileScope.APPLICATION);
    verifyStatic();
    Localytics.setProfileAttribute("email", "baz", Localytics.ProfileScope.APPLICATION);
    verifyStatic();
    Localytics.setProfileAttribute("name", "bar", Localytics.ProfileScope.APPLICATION);
    verifyStatic();
    Localytics.setProfileAttribute("custom", "qaz", Localytics.ProfileScope.APPLICATION);
  }

  @Test public void identifyWithCustomDimensions() {
    integration.customDimensions = new ValueMap().putValue("foo", 1);

    integration.identify(new IdentifyPayloadBuilder() //
        .traits(createTraits("bar").putValue("foo", "baz")).build());

    verifyStatic();
    Localytics.setCustomerId("bar");
    verifyStatic();
    Localytics.setCustomDimension(1, "baz");
    verifyStatic();
    Localytics.setProfileAttribute("userId", "bar", Localytics.ProfileScope.APPLICATION);
    verifyStatic();
    Localytics.setProfileAttribute("foo", "baz", Localytics.ProfileScope.APPLICATION);
  }

  @Test public void group() {
    integration.group(new GroupPayloadBuilder().build());
  }

  @Test public void flush() {
    integration.flush();
    verifyStatic();
    Localytics.upload();
  }

  @Test public void screen() {
    integration.screen(new ScreenPayloadBuilder().category("foo").name("bar").build());
    verifyStatic();
    Localytics.tagScreen("bar");

    integration.screen(new ScreenPayloadBuilder().name("baz").build());
    verifyStatic();
    Localytics.tagScreen("baz");

    integration.screen(new ScreenPayloadBuilder().category("qux").build());
    verifyStatic();
    Localytics.tagScreen("qux");
  }

  @Test public void track() {
    integration.track(new TrackPayloadBuilder().event("foo").build());
    verifyStatic();
    Localytics.tagEvent("foo", new HashMap<String, String>());
  }

  @Test public void trackWithRevenue() {
    Properties props = new Properties().putRevenue(20);
    integration.track(new TrackPayloadBuilder().event("bar").properties(props).build());
    verifyStatic();
    Localytics.tagEvent("bar", props.toStringMap(), 2000);
  }

  @Test public void trackWithCustomDimensions() {
    integration.customDimensions = new ValueMap().putValue("foo", 9);

    Properties props = new Properties().putValue("foo", 1);
    integration.track(new TrackPayloadBuilder().event("bar").properties(props).build());

    verifyStatic();
    Localytics.tagEvent("bar", props.toStringMap());
    verifyStatic();
    Localytics.setCustomDimension(9, "1");
  }
}

