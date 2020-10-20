/*
 * Copyright 2020 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.servlet.parameters;

import com.linkedin.kafka.cruisecontrol.config.KafkaCruiseControlConfig;
import com.linkedin.kafka.cruisecontrol.config.constants.ExecutorConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;


public class ParameterUtilsTest {

  private static final Long DEFAULT_START_TIME_MS = -1L;
  private static final Long DEFAULT_END_TIME_MS = -2L;
  private static final String START_TIME_STRING = "12345";
  private static final String END_TIME_STRING = "23456";
  private static final String REPLICATION_THROTTLE_STRING = "1000";
  private static final String DEFAULT_REPLICATION_THROTTLE_STRING = "2000";
  private static final String EXECUTION_PROGRESS_CHECK_INTERVAL_STRING = "1500";

  @Test
  public void testParseTimeRangeSet() {
    HttpServletRequest mockRequest = EasyMock.mock(HttpServletRequest.class);
    Map<String, String[]> paramMap = new HashMap<>();
    paramMap.put(ParameterUtils.START_MS_PARAM, new String[]{ParameterUtils.START_MS_PARAM});
    paramMap.put(ParameterUtils.END_MS_PARAM, new String[]{ParameterUtils.END_MS_PARAM});

    EasyMock.expect(mockRequest.getParameterMap()).andReturn(paramMap).times(4);
    EasyMock.expect(mockRequest.getParameter(ParameterUtils.START_MS_PARAM)).andReturn(START_TIME_STRING).times(2);
    EasyMock.expect(mockRequest.getParameter(ParameterUtils.END_MS_PARAM)).andReturn(END_TIME_STRING).times(2);
    EasyMock.replay(mockRequest);

    Long startMs = ParameterUtils.startMsOrDefault(mockRequest, null);
    Long endMs = ParameterUtils.endMsOrDefault(mockRequest, null);
    Assert.assertNotNull(startMs);
    Assert.assertNotNull(endMs);
    Assert.assertEquals(Long.valueOf(START_TIME_STRING), startMs);
    Assert.assertEquals(Long.valueOf(END_TIME_STRING), endMs);
    ParameterUtils.validateTimeRange(startMs, endMs);

    startMs = ParameterUtils.startMsOrDefault(mockRequest, DEFAULT_START_TIME_MS);
    endMs = ParameterUtils.endMsOrDefault(mockRequest, DEFAULT_END_TIME_MS);

    Assert.assertEquals(Long.valueOf(START_TIME_STRING), startMs);
    Assert.assertEquals(Long.valueOf(END_TIME_STRING), endMs);
  }

  @Test
  public void testParseTimeRangeNotSet() {
    HttpServletRequest mockRequest = EasyMock.mock(HttpServletRequest.class);
    // Mock the request so that it does not contain start/end time
    EasyMock.expect(mockRequest.getParameterMap()).andReturn(Collections.emptyMap()).times(4);
    EasyMock.replay(mockRequest);

    Long startMs = ParameterUtils.startMsOrDefault(mockRequest, null);
    Long endMs = ParameterUtils.endMsOrDefault(mockRequest, null);
    Assert.assertNull(startMs);
    Assert.assertNull(endMs);

    startMs = ParameterUtils.startMsOrDefault(mockRequest, DEFAULT_START_TIME_MS);
    endMs = ParameterUtils.endMsOrDefault(mockRequest, DEFAULT_END_TIME_MS);

    Assert.assertEquals(DEFAULT_START_TIME_MS, startMs);
    Assert.assertEquals(DEFAULT_END_TIME_MS, endMs);
  }

  @Test
  public void testParseReplicationThrottleWithNoDefault() {
    HttpServletRequest mockRequest = EasyMock.mock(HttpServletRequest.class);
    KafkaCruiseControlConfig controlConfig = EasyMock.mock(KafkaCruiseControlConfig.class);

    Map<String, String[]> paramMap = new HashMap<>();
    paramMap.put(ParameterUtils.REPLICATION_THROTTLE_PARAM, new String[]{ParameterUtils.REPLICATION_THROTTLE_PARAM});

    EasyMock.expect(mockRequest.getParameterMap()).andReturn(paramMap).times(1);
    EasyMock.expect(mockRequest.getParameter(ParameterUtils.REPLICATION_THROTTLE_PARAM)).andReturn(REPLICATION_THROTTLE_STRING).times(1);
    EasyMock.expect(controlConfig.getLong(ExecutorConfig.DEFAULT_REPLICATION_THROTTLE_CONFIG)).andReturn(null); // No default

    EasyMock.replay(mockRequest);
    EasyMock.replay(controlConfig);

    Long replicationThrottle = ParameterUtils.replicationThrottle(mockRequest, controlConfig);
    Assert.assertEquals(Long.valueOf(REPLICATION_THROTTLE_STRING), replicationThrottle);
  }

  @Test
  public void testParseReplicationThrottleWithDefault() {
    HttpServletRequest mockRequest = EasyMock.mock(HttpServletRequest.class);
    KafkaCruiseControlConfig controlConfig = EasyMock.mock(KafkaCruiseControlConfig.class);
    // No parameter string value in the parameter map
    EasyMock.expect(mockRequest.getParameterMap()).andReturn(Collections.emptyMap()).times(1);
    EasyMock.expect(controlConfig.getLong(ExecutorConfig.DEFAULT_REPLICATION_THROTTLE_CONFIG))
            .andReturn(Long.valueOf(DEFAULT_REPLICATION_THROTTLE_STRING));

    EasyMock.replay(mockRequest);
    EasyMock.replay(controlConfig);

    Long replicationThrottle = ParameterUtils.replicationThrottle(mockRequest, controlConfig);
    Assert.assertEquals(Long.valueOf(DEFAULT_REPLICATION_THROTTLE_STRING), replicationThrottle);
  }

  @Test
  public void testParseExecutionProgressCheckIntervalMsNoValue() {
    HttpServletRequest mockRequest = EasyMock.mock(HttpServletRequest.class);
    EasyMock.expect(mockRequest.getParameterMap()).andReturn(Collections.emptyMap()).times(1);
    EasyMock.replay(mockRequest);
    Assert.assertNull(ParameterUtils.executionProgressCheckIntervalMs(mockRequest));
  }

  @Test
  public void testParseExecutionProgressCheckIntervalMsWithValue() {
    HttpServletRequest mockRequest = EasyMock.mock(HttpServletRequest.class);

    Map<String, String[]> paramMap = new HashMap<>();
    paramMap.put(
        ParameterUtils.EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM,
        new String[]{ParameterUtils.EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM});

    EasyMock.expect(mockRequest.getParameterMap()).andReturn(paramMap).times(1);
    EasyMock.expect(mockRequest.getParameter(ParameterUtils.EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM))
            .andReturn(EXECUTION_PROGRESS_CHECK_INTERVAL_STRING).times(1);

    EasyMock.replay(mockRequest);

    Long executionProgressCheckIntervalMs = ParameterUtils.executionProgressCheckIntervalMs(mockRequest);
    Assert.assertEquals(Long.valueOf(EXECUTION_PROGRESS_CHECK_INTERVAL_STRING), executionProgressCheckIntervalMs);
  }
}
