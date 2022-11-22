package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws IOException {

    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter crawlResultWriter = new CrawlResultWriter(result);

    if (!config.getResultPath().isEmpty()) {
      Path path = Paths.get(config.getResultPath());
      crawlResultWriter.write(path);
    } else {
      Writer writer = new OutputStreamWriter(System.out.printf(result.toString()));
      crawlResultWriter.write(writer);
    }

    if (!config.getProfileOutputPath().isEmpty()) {
      Path path = Paths.get(config.getProfileOutputPath());
      profiler.writeData(path);
    } else {
      Writer writer = new OutputStreamWriter(System.out);
      profiler.writeData(writer);
      writer.flush();
    }

  }

  public static void main(String[] args) throws IOException {

    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

    CrawlerConfiguration crawlerConfiguration = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(crawlerConfiguration).run();
  }
}
