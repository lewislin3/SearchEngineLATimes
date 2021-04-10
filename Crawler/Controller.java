import java.io.BufferedReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.FileReader;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {

	public static void main(String[] args) throws Exception {
		
		
		 
		 String crawlStorageFolder = "/data/crawl";
		 int numberOfCrawlers = 8;
		 
		 CrawlConfig config = new CrawlConfig();
		 config.setCrawlStorageFolder(crawlStorageFolder);
		 config.setMaxDepthOfCrawling(16);
	     config.setMaxPagesToFetch(20000);
	     config.setIncludeBinaryContentInCrawling(true);

		 /*
		 * Instantiate the controller for this crawl.
		 */
		 PageFetcher pageFetcher = new PageFetcher(config);
		 RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		 robotstxtConfig.setEnabled(false);
		 RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		 CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		 /*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		 controller.addSeed("https://www.latimes.com/");
		
		 /*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		 
		 //controller.start(MyCrawler.class, numberOfCrawlers);
		 try (FileWriter TxtWriter = new FileWriter("CrawlReport_latimes.txt")) {
			 TxtWriter.append("Name: Yu-Hsin Lin\n");
			 TxtWriter.append("USC ID: 7206698184\n");
			 TxtWriter.append("News site crawled: latimes.com\n");
			 TxtWriter.append("Number of threads: 8\n\n");
			 TxtWriter.flush();
			 TxtWriter.append("Fetch Statistics\n================\n");
			 int fetched = 0;
			 int success = 0;
			 int failed = 0;
			 Map<String,Integer> httpMap = new HashMap<>();
			 Map<String,Integer> fMap = new HashMap<>();
			 BufferedReader csvReader = new BufferedReader(new FileReader("fetch_latimes.csv"));
			 String row;
			 while ((row = csvReader.readLine()) != null) {
			     String[] data = row.split(",");
			     if(data[0].equals("URL"))continue;
			     fetched++;
			     fMap.put(data[0],Integer.valueOf(data[1]));
			     if(Integer.valueOf(data[1]) / 100 == 2) success++;
			     else failed++;
			     Integer tmp = httpMap.get(data[1]);
			     if(tmp == null) httpMap.put(data[1], 1); 
			     else httpMap.put(data[1], tmp+1);
			 }
			 TxtWriter.append("Fetch attemped:"+ String.valueOf(fetched) + "\n");
			 TxtWriter.append("Fetch successed: "+ String.valueOf(success) + "\n");
			 TxtWriter.append("Fetch failed or aborted: "+ String.valueOf(failed) + "\n");
			 TxtWriter.flush();
			 csvReader.close();
			 TxtWriter.append("\nOutgoing URLs\n================\n");
			 int urls = 0;
			 int out = 0;
			 int in = 0;
			 Set<String> urlSet = new HashSet<>();
			 csvReader = new BufferedReader(new FileReader("urls_latimes.csv"));
			 while ((row = csvReader.readLine()) != null) {
			     String[] data = row.split(",");
			     if(data[0].equals("URL"))continue;
				 urls++;
			     if(!urlSet.contains(data[0])) {
			    	 if(data[0].startsWith("https://www.latimes.com/") || data[0].startsWith("http://www.latimes.com/")) in++;
			    	 else out++;
			    	 urlSet.add(data[0]);
			     }
			 }
			 TxtWriter.append("Total URLs extracted: "+ String.valueOf(urls) + "\n");
			 TxtWriter.append("# unique URLs extracted: "+ String.valueOf(urlSet.size()) + "\n");
			 TxtWriter.append("# unique URLs within News Site: "+ String.valueOf(in) + "\n");
			 TxtWriter.append("# unique URLs outside News Site: "+ String.valueOf(out) + "\n");
			 TxtWriter.append("\nStatus Codes:\n" + "=============\n");
			 for(String key: httpMap.keySet()) {
				 TxtWriter.append(key);
				 if(key.equals("200"))TxtWriter.append(" Ok");
				 if(key.equals("301"))TxtWriter.append(" Moved Permanently");
				 if(key.equals("401"))TxtWriter.append(" Unauthorized");
				 if(key.equals("403"))TxtWriter.append(" Forbidden");
				 if(key.equals("404"))TxtWriter.append(" Not Found");
				 TxtWriter.append(": " + httpMap.get(key) + '\n');
			 }
			 TxtWriter.flush();
			 csvReader.close();
			
			 csvReader = new BufferedReader(new FileReader("visit_latimes.csv"));
			 int size1kb = 0;
			 int size10kb = 0;
			 int size100kb = 0;
			 int size1mb = 0;
			 int sizemax = 0;
			 Map<String,Integer> typeMap = new HashMap<>();
			 while ((row = csvReader.readLine()) != null) {
			     String[] data = row.split(",");
			     if(data[0].equals("URL"))continue;
			     int size = Integer.valueOf(data[1]);
			     if(size < 1024)size1kb++;
			     else if(size < 10240)size10kb++;
			     else if(size < 102400)size100kb++;
			     else if(size < 1048576)size1mb++;
			     else sizemax++;
			     Integer tmp = typeMap.get(data[3]);
			     if(tmp == null) typeMap.put(data[3], 1); 
			     else typeMap.put(data[3], tmp+1);
			 }
			 TxtWriter.append("\nFile Sizes:\n" + "=============\n");
			 TxtWriter.append("< 1KB: "+ String.valueOf(size1kb) + "\n");
			 TxtWriter.append("1KB ~ <10KB: "+ String.valueOf(size10kb) + "\n");
			 TxtWriter.append("10KB ~ <100KB: "+ String.valueOf(size100kb) + "\n");
			 TxtWriter.append("100KB ~ <1MB: "+ String.valueOf(size1mb) + "\n");
			 TxtWriter.append(">=1MB: "+ String.valueOf(sizemax) + "\n");
			 TxtWriter.append("\nContent Types:\n" + "=============\n");
			 for(String key: typeMap.keySet()) {
				 int tmp = typeMap.get(key);
				 TxtWriter.append(key+ ": " + String.valueOf(tmp) + '\n');
			 }
			 TxtWriter.flush();
			 csvReader.close();
		}
		 
	}

}
