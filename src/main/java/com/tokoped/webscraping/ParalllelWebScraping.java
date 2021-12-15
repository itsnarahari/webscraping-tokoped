package com.tokoped.webscraping;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParalllelWebScraping {

    static CsvMapper mapper = new CsvMapper();
    public static final int PAGES=20;
    public static File csvOutputFile = new File("tokopedTop100Products.csv");
    private final static Logger LOGGER =
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static String webScraping() throws IOException, ExecutionException, InterruptedException {

        CsvSchema schema = CsvSchema.builder().setUseHeader(true)
                .addColumn("name")
                .addColumn("description")
                .addColumn("image")
                .addColumn("price")
                .addColumn("store")
                .build();

        ObjectWriter writer = mapper.writerFor(CSVHeaders.class).with(schema);

        List<CompletableFuture<List<CSVHeaders>>> futures = new ArrayList();
        List<CSVHeaders> list = new ArrayList<>();

        for (int i = 1; i <= PAGES; i++) {
            int finalI1 = i;
            CompletableFuture<List<CSVHeaders>> requestCompletableFuture = CompletableFuture
                    .supplyAsync(
                            () ->
                            {
                                try {
                                    return exec(finalI1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                    );

            futures.add(requestCompletableFuture);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null)
                .join();
        for (CompletableFuture<List<CSVHeaders>> eachFuture : futures)
            list.addAll(eachFuture.get());

        LOGGER.log(Level.INFO, "Started processing the file");
        writer.writeValues(csvOutputFile).writeAll(list);
        LOGGER.log(Level.INFO, "Content was written to csv");
        return "success";
    }
    public static List<CSVHeaders> exec(int i) throws IOException {

        String pageUrl = String.format("https://www.tokopedia.com/p/handphone-tablet/handphone?page=%d", i);
        LOGGER.log(Level.INFO, String.format("Processing the page %s", pageUrl));

        Document doc = Jsoup.connect(pageUrl).get();
        Elements container = doc.select("div.css-13l3l78.e1nlzfl10");
        Elements products = container.select("div.css-bk6tzz.e1nlzfl3");
        List<CSVHeaders> list = new ArrayList<>();
        for (Element product : products) {
            CSVHeaders csvHeaders = new CSVHeaders();
            csvHeaders.setName(product.select("span.css-1bjwylw").text());
            csvHeaders.setDescription(product.select("span.css-1bjwylw").text());
            Elements image = product.select("div.css-1c0vu8l");
            Element img = image.select("img").first();
            String src = img.attr("src");
            csvHeaders.setImage(src);
            csvHeaders.setPrice(product.select("span.css-o5uqvq").text());
            csvHeaders.setStore(product.select("span.css-1kr22w3").text());
            list.add(csvHeaders);
        }
        LOGGER.log(Level.INFO, String.format("Processed @ %d page index", i));
        return list;

    }
}
