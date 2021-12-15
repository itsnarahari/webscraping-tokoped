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

public class WebScraping {
    static CsvMapper mapper = new CsvMapper();
    public static final int PAGES=10;

    public static String webScraping() throws IOException {

        File csvOutputFile = new File("tokopedTop100Products.csv");

        List<CSVHeaders> list = new ArrayList<>();

        CsvSchema schema = CsvSchema.builder().setUseHeader(true)
                .addColumn("name")
                .addColumn("description")
                .addColumn("image")
                .addColumn("price")
                .addColumn("store")
                .build();

        ObjectWriter writer = mapper.writerFor(CSVHeaders.class).with(schema);

        for (int i = 1; i < PAGES; i++) {
            Document doc = Jsoup.connect(String.format("https://www.tokopedia.com/p/handphone-tablet/handphone?page='%d'", i)).get();
            Elements container = doc.select("div.css-13l3l78.e1nlzfl10");
            Elements products = container.select("div.css-bk6tzz.e1nlzfl3");
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
                writer.writeValues(csvOutputFile).writeAll(list);
            }
        }
        return "success";
    }
}
