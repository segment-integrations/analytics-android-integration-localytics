package com.segment.analytics.android.integrations.localytics;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import com.localytics.android.Localytics;
import com.segment.analytics.Analytics;
import com.segment.analytics.AnalyticsContext;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;
import java.util.Collections;
import java.util.Map;

import static com.segment.analytics.Analytics.LogLevel;
import static com.segment.analytics.internal.Utils.isNullOrEmpty;
import static com.segment.analytics.internal.Utils.isOnClassPath;

/**
 * Localytics is a general-purpose mobile analytics tool that measures customer acquisition, ad
 * attribution, retargeting campaigns and user actions in your mobile apps.
 *
 * @see <a href="http://www.localytics.com/">Localytics</a>
 * @see <a href="https://segment.com/docs/integrations/localytics/">Localytics Integration</a>
 * @see <a href="http://www.localytics.com/docs/android-integration/">Localytics Android SDK</a>
 */
public class LocalyticsIntegration extends Integration<Void> {
  public static final Factory FACTORY = new Factory() {
    @Override public Integration<?> create(ValueMap settings, Analytics analytics) {
      return new LocalyticsIntegration(analytics, settings);
    }

    @Override public String key() {
      return LOCALYTICS_KEY;
    }
  };
  private static final String LOCALYTICS_KEY = "Localytics";

  final Logger logger;
  final boolean hasSupportLibOnClassPath;
  final boolean organizationScope;
  final Localytics.ProfileScope attributeScope;
  ValueMap customDimensions;

  LocalyticsIntegration(Analytics analytics, ValueMap settings) {
    logger = analytics.logger(LOCALYTICS_KEY);

    // Localytics has pretty verbose logging output, so use the verbose level.
    // RefL https://github.com/segmentio/analytics-android/pull/388/files
    boolean loggingEnabled = logger.logLevel.ordinal() >= LogLevel.VERBOSE.ordinal();
    Localytics.setLoggingEnabled(loggingEnabled);
    logger.verbose("Localytics.setLoggingEnabled(%s);", loggingEnabled);

    String appKey = settings.getString("appKey");
    Localytics.integrate(analytics.getApplication(), appKey);
    logger.verbose("Localytics.integrate(context, %s);", appKey);

    hasSupportLibOnClassPath = isOnClassPath("android.support.v4.app.FragmentActivity");
    customDimensions = settings.getValueMap("dimensions");
    if (customDimensions == null) {
      customDimensions = new ValueMap(Collections.<String, Object>emptyMap());
    }

    organizationScope = settings.getBoolean("setOrganizationScope", false);
    if (organizationScope) {
      attributeScope = Localytics.ProfileScope.ORGANIZATION;
    } else {
      attributeScope = Localytics.ProfileScope.APPLICATION;
    }
  }

  @Override public void onActivityResumed(Activity activity) {
    super.onActivityResumed(activity);

    Localytics.openSession();
    logger.verbose("Localytics.openSession();");

    Localytics.upload();
    logger.verbose("Localytics.upload();");

    if (hasSupportLibOnClassPath) {
      if (activity instanceof android.support.v4.app.FragmentActivity) {
        Localytics.setInAppMessageDisplayActivity(
            (android.support.v4.app.FragmentActivity) activity);
        logger.verbose("Localytics.setInAppMessageDisplayActivity(activity);");
      }
    }

    Intent intent = activity.getIntent();
    if (intent != null) {
      Localytics.handleTestMode(intent);
      logger.verbose("Localytics.handleTestMode(%s);", intent);
    }
  }

  @Override public void onActivityPaused(Activity activity) {
    super.onActivityPaused(activity);

    if (hasSupportLibOnClassPath) {
      if (activity instanceof android.support.v4.app.FragmentActivity) {
        Localytics.dismissCurrentInAppMessage();
        logger.verbose("Localytics.dismissCurrentInAppMessage();");
        Localytics.clearInAppMessageDisplayActivity();
        logger.verbose("Localytics.clearInAppMessageDisplayActivity();");
      }
    }

    Localytics.closeSession();
    logger.verbose("Localytics.closeSession();");
    Localytics.upload();
    logger.verbose("Localytics.upload();");
  }

  @Override public void flush() {
    super.flush();

    Localytics.upload();
    logger.verbose("Localytics.upload();");
  }

  @Override public void identify(IdentifyPayload identify) {
    super.identify(identify);

    setContext(identify.context());
    Traits traits = identify.traits();

    String userId = identify.userId();
    if (!isNullOrEmpty(userId)) {
      Localytics.setCustomerId(userId);
      logger.verbose("Localytics.setCustomerId(%s);", userId);
    }

    String email = traits.email();
    if (!isNullOrEmpty(email)) {
      Localytics.setIdentifier("email", email);
      Localytics.setCustomerEmail(email);
      logger.verbose("Localytics.setIdentifier(\"email\", %s);", email);
      logger.verbose("Localytics.setCustomerEmail(\"$email\", %s);", email);
    }

    String name = traits.name();
    if (!isNullOrEmpty(name)) {
      Localytics.setIdentifier("customer_name", name);
      Localytics.setCustomerFullName(name);
      logger.verbose("Localytics.setIdentifier(\"customer_name\", %s);", name);
      logger.verbose("Localytics.setFullName(\"$full_name\", %s);", name);
    }

    String firstName = traits.firstName();
    if (!isNullOrEmpty(firstName)) {
      Localytics.setCustomerFirstName(firstName);
      logger.verbose("Localytics.setCustomerFirstName(\"$first_name\", %s);", firstName);
    }

    String lastName = traits.lastName();
    if (!isNullOrEmpty(lastName)) {
      Localytics.setCustomerLastName(lastName);
      logger.verbose("Localytics.setCustomerLastName(\"$first_name\", %s);", firstName);
    }

    setCustomDimensions(traits);

    for (Map.Entry<String, Object> entry : traits.entrySet()) {
      String key = entry.getKey();
      String value = String.valueOf(entry.getValue());
      Localytics.setProfileAttribute(key, value, attributeScope);
      logger.verbose("Localytics.setProfileAttribute(%s, %s, %s);", key, value, attributeScope);
    }
  }

  @Override public void screen(ScreenPayload screen) {
    super.screen(screen);

    setContext(screen.context());

    String event = screen.event();
    Localytics.tagScreen(event);
    logger.verbose("Localytics.tagScreen(%s);", event);
  }

  @Override public void track(TrackPayload track) {
    super.track(track);
    setContext(track.context());

    String event = track.event();
    Properties properties = track.properties();
    Map<String, String> stringProps = properties.toStringMap();
    // Convert revenue to cents.
    // http://docs.localytics.com/index.html#Dev/Instrument/customer-ltv.html
    final long revenue = (long) (properties.revenue() * 100);

    if (revenue != 0) {
      Localytics.tagEvent(event, stringProps, revenue);
      logger.verbose("Localytics.tagEvent(%s, %s, %s);", event, stringProps, revenue);
    } else {
      Localytics.tagEvent(event, stringProps);
      logger.verbose("Localytics.tagEvent(%s, %s);", event, stringProps);
    }

    setCustomDimensions(properties);
  }

  private void setContext(AnalyticsContext context) {
    if (isNullOrEmpty(context)) {
      return;
    }

    AnalyticsContext.Location location = context.location();
    if (location != null) {
      Location androidLocation = new Location("Segment");
      androidLocation.setLongitude(location.longitude());
      androidLocation.setLatitude(location.latitude());
      androidLocation.setSpeed((float) location.speed());
      Localytics.setLocation(androidLocation);
      logger.verbose("Localytics.setLocation(%s);", androidLocation);
    }
  }

  private void setCustomDimensions(ValueMap dimensions) {
    for (Map.Entry<String, Object> entry : dimensions.entrySet()) {
      String dimensionKey = entry.getKey();
      if (customDimensions.containsKey(dimensionKey)) {
        int dimension = customDimensions.getInt(dimensionKey, 0);
        String value = String.valueOf(entry.getValue());
        Localytics.setCustomDimension(dimension, value);
        logger.verbose("Localytics.setCustomDimension(%s, %s);", dimension, value);
      }
    }
  }
}
