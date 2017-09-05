package shadon.technologies.app.craigslistdiffchecker.service;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shadon.technologies.app.craigslistdiffchecker.craigsObjects.CraigslistAd;
import shadon.technologies.app.craigslistdiffchecker.craigsObjects.SavedSearch;
import shadon.technologies.app.craigslistdiffchecker.files.FileIO;
import shadon.technologies.app.craigslistdiffchecker.files.Paths;
import shadon.technologies.app.craigslistdiffchecker.network.NetworkCommunication;

/**
 * Created by Maveric on 6/25/2016.
 */
public class LinkCheck {

    private static final String TAG = "LinkCheck";

    AndroidBackgroundService service;

    public static ArrayList<CraigslistAd> CheckSaleLinks(AndroidBackgroundService service, SavedSearch search){

        Log.i(TAG, "RUNNING SEARCH NAMED: " + search.name);
        Log.d(TAG, "Search url: " + search.url);

        ArrayList<CraigslistAd> listCraigslistPageLinks;
        ArrayList<CraigslistAd> listCraigslistAds;

        File linkCacheFolder = new File(Paths.cachedSearchesFileLocation);
        linkCacheFolder.getParentFile().mkdirs();
        ArrayList<String> listOldSearches = FileIO.readFile(linkCacheFolder);

        if(listOldSearches == null){
            listOldSearches = new ArrayList<>();
        }

        listCraigslistPageLinks = readAllLinksFromPageSource(search);
        if (listCraigslistPageLinks == null) {
            Log.e(TAG, "listCraigslistPageLinks is null. Returning without checking for new links.");
            NetworkCommunication.writeLogsToS3(service);
            return null;
        }

        listCraigslistAds = findAdLinks(listCraigslistPageLinks);

        ArrayList<CraigslistAd> listNewAds = findNewLinks(listCraigslistAds, listOldSearches);
        if(listNewAds != null) {
            FileIO.writeLinksFile(listNewAds);
        }
        return listNewAds;
    }

    static private ArrayList<CraigslistAd> findAdLinks(ArrayList<CraigslistAd> listAllPageLinks) {

        ArrayList<CraigslistAd> listCraigslistAds = new ArrayList<>();

        for (CraigslistAd craigslistAd : listAllPageLinks) {

            Pattern p = Pattern.compile(".*craigslist.org/.../.*.html");
            Matcher m = p.matcher(craigslistAd.url);
            if (m.matches()) {
                listCraigslistAds.add(craigslistAd);
            }
        }

        return listCraigslistAds;

    }

    static private ArrayList<CraigslistAd> readAllLinksFromPageSource(SavedSearch search) {

        Connection.Response html;
        Document document = null;

        Connection jsoup = Jsoup.connect(search.url);
        jsoup.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36");
        try {
            html = jsoup.execute();
            document = html.parse();
        } catch (IOException e) {
            Log.e(TAG, "Unable to contact the website!!!");
            e.printStackTrace();
            return null;
        }

        Elements links = document.select("a");

        ArrayList<CraigslistAd> listLinksFoundOnPage = new ArrayList<>();

        for (Element e : links) {
            if (e.childNodes().size() == 1){
                CraigslistAd craigslistAd = new CraigslistAd();
                craigslistAd.url = e.attr("abs:href");
                craigslistAd.title = ((TextNode) e.childNode(0)).text();
                listLinksFoundOnPage.add(craigslistAd);
            }
        }

        return listLinksFoundOnPage;
    }

    private static ArrayList<CraigslistAd> findNewLinks(ArrayList<CraigslistAd> listNewCriagslistAds, ArrayList<String> listSavedSaleUrls) {

        Log.d(TAG, "List of new links:");

        for(CraigslistAd ad : listNewCriagslistAds){
            Log.d(TAG, ad.url);
        }

        Log.d(TAG, "List of old links:");

        for(String s : listSavedSaleUrls){
            Log.d(TAG, s);
        }

        ArrayList<CraigslistAd> listUnseenCraigslistAds = new ArrayList<>(listNewCriagslistAds);

        for (CraigslistAd ad : listNewCriagslistAds) {
            for (String savedUrl : listSavedSaleUrls) {
                if (ad.url.equals(savedUrl)) {
                    listUnseenCraigslistAds.remove(ad);
                    continue;
                }
            }
        }

        if (listUnseenCraigslistAds.size() > 0) {
            Log.i(TAG, "New ads found. Printing out all link differences");
            for (CraigslistAd ad : listUnseenCraigslistAds) {
                Log.i(TAG, ad.url);
            }
            return listUnseenCraigslistAds;
        } else {
            Log.i(TAG, "No new links found");
            return null;
        }
    }
}
