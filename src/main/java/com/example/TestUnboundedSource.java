package com.example;

import java.io.IOException;

import org.joda.time.Duration;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.io.PubsubIO;
import com.google.cloud.dataflow.sdk.io.Read;
import com.google.cloud.dataflow.sdk.io.TextIO;
import com.google.cloud.dataflow.sdk.io.Write;
import com.google.cloud.dataflow.sdk.options.DataflowPipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.runners.DataflowPipelineRunner;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.transforms.windowing.AfterProcessingTime;
import com.google.cloud.dataflow.sdk.transforms.windowing.AfterWatermark;
import com.google.cloud.dataflow.sdk.transforms.windowing.FixedWindows;
import com.google.cloud.dataflow.sdk.transforms.windowing.Window;
import com.google.cloud.dataflow.sdk.values.KV;
import com.google.cloud.dataflow.sdk.values.PCollection;

public class TestUnboundedSource {

	static {

	    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
	}
	
	public static void main(String[] args) throws IOException {
		DataflowPipelineOptions options = PipelineOptionsFactory.create().as(DataflowPipelineOptions.class);
		/*
		options.setRunner(DataflowPipelineRunner.class);
		options.setProject("rapid-stream-118713");
		options.setStagingLocation("gs://streaming-test");
		options.setStreaming(Boolean.TRUE);
		options.setMaxNumWorkers(1);
		options.setNumWorkers(1);
		*/
		Pipeline p = Pipeline.create(options);
		p.begin();
		TUnboundedSource source = new TUnboundedSource();
		PCollection<String> input = p.apply(Read.from(source).named("TUnboundedSource").withMaxNumRecords(100));
		PCollection<String> windowInput = input
				// .apply(Window.<String>
				// into(FixedWindows.of(Duration.standardMinutes(1))));
				.apply(Window.<String> into(FixedWindows.of(Duration.standardSeconds(10)))
						.triggering(
								AfterWatermark.pastEndOfWindow()
								//.plusDelayOf(Duration.standardMinutes(1))
								)
						.discardingFiredPanes().withAllowedLateness(Duration.ZERO));
		
		windowInput.apply(TextIO.Write.to("gs://synpuf_data/streaming.txt"));
		p.run();

	}

}
