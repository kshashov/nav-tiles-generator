package org.github.kshashov;

import com.sun.source.tree.BreakTree;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
//        double x0 = 50.1759;
//        double y0 = 53.2170; big
        double x0 = 50.1321;
        double y0 = 53.1999;
        double xDelta = 0.01;
        double yDelta = 0.01;
        int tileWidth = 159; // TODO
        int tileHeight = 265; // TODO
        int columns = 15;
        int rows = 10;
        double x1 = x0 - (columns/2) * xDelta;
        double y1 = y0 - (rows/2) * yDelta;
        double x2 = x0 + (columns/2) * xDelta;
        double y2 = y0 + (rows/2) * yDelta;
        CookieManager cookieHandler = new CookieManager();
        HttpCookie cookie = new HttpCookie("_osm_totp_token", "271407");
        cookie.setPath("/");
        cookie.setVersion(0);
        cookie.setDomain("https://render.openstreetmap.org");
        cookieHandler.getCookieStore().add(new URI("https://render.openstreetmap.org"), cookie);
        HttpClient httpClient = HttpClient.newBuilder().cookieHandler(cookieHandler).build();

        StringBuilder tiles = new StringBuilder().append("[");

        for (int j = 0; j < rows; j++) {

            tiles.append("[");

            for (int i = 0; i < columns; i++) {
                try {
                    double left = x1 + (i ) * xDelta;
                    double top = y2 - (j) * yDelta;
                    double right = x1 + (i + 1) * xDelta;
                    double bottom = y2 - (j + 1) * yDelta;
                    URI uri = new URI("https://render.openstreetmap.org/cgi-bin/export?bbox=" + left + "," + bottom + "," + right + "," + top + "&scale=25000&format=png");
                    HttpResponse<byte[]> response = httpClient.send(HttpRequest.newBuilder()
                            .uri(uri)
                            .GET()
                            .timeout(Duration.ofMinutes(5))
//                        .build(), new HttpResponse.BodyHandler<Path>() {
//                    @Override
//                    public HttpResponse.BodySubscriber<Path> apply(HttpResponse.ResponseInfo responseInfo) {
//                        return null;
//                    }
//                });
                            .build(), HttpResponse.BodyHandlers.ofByteArray());

                    String imgName =  "map" + j + "_" + i + ".png";
                    File outputFile = new File("E:/tiles/" + imgName);
                    Files.write(outputFile.toPath(), response.body());

                    if (i == 0 && j == 0) {
                        BufferedImage bimg = ImageIO.read(outputFile);
                        tileWidth = bimg.getWidth();
                        tileHeight = bimg.getHeight();
                    }

                    tiles.append(String.format(Locale.US,"""
                {
                    top: %f,
                    left: %f,
                    bottom: %f,
                    right: %f,
                    image: '%s',
                    row: %d,
                    column: %d,
                }
                """, top, left, bottom, right, tileWidth, tileHeight, "map" + j + "_" + i + ".png", i, j, Math.abs(right-left), Math.abs(top-bottom)));

                    if (i < columns - 1) {
                        tiles.append(",");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }

            tiles.append("]");

            if (j < rows - 1) {
                tiles.append(",");
            }
        }

        tiles.append("]");

        //System.out.println(tiles);

        StringBuilder map = new StringBuilder();
        map.append(String.format(Locale.US,"""
                {
                    top: %f,
                    left: %f,
                    bottom: %f,
                    right: %f,
                    width: %d,
                    height: %d,
                    tileWidth: %d,
                    tileHeight: %d,
                    tileLongWidth: %f,
                    tileLatHeight: %f
                }
                
                """, y2, x1, y1, x2, tileWidth*columns, tileHeight*rows, tileWidth, tileHeight, xDelta, yDelta));

        System.out.println(map);
    }

}