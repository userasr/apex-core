/*
 *  Copyright (c) 2012-2013 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.stream;

import com.malhartech.api.Sink;
import com.malhartech.netlet.Client.Fragment;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chetan Narsude <chetan@malhar-inc.com>
 */
public class BufferServerSubscriberTest
{
  @Test
  public void testEmergencySinks() throws InterruptedException
  {
    final List<Object> list = new ArrayList<Object>();
    Sink<Object> unbufferedSink = new Sink<Object>()
    {
      Object o;
      int i;

      @Override
      public void process(Object tuple)
      {
        if (o == null) {
          o = tuple;
        }
        else {
          list.add(o);
          o = null;
          throw new IllegalStateException("Buffer full");
        }
      }

    };

    BufferServerSubscriber bss = new BufferServerSubscriber("subscriber")
    {
      @Override
      public void suspendRead()
      {
        logger.debug("read suspended");
      }

      @Override
      public void resumeRead()
      {
        logger.debug("read resumed");
      }

    };
    bss.setSink("unbufferedSink", unbufferedSink);
    bss.activateSinks();

    int i;
    for (i = 0; i < 10; i++) {
      bss.distribute(new Fragment(new byte[] {(byte)i}, 0 , 1));
    }

    bss.endMessage();
    Thread.sleep(20);
    Assert.assertTrue("tuples received", i - 1 <= list.size());
  }

  private static final Logger logger = LoggerFactory.getLogger(BufferServerSubscriberTest.class);
}