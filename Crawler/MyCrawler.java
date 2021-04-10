import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

public class MyCrawler extends WebCrawler {
	private static FileWriter urlWriter;
	private static FileWriter fetchWriter;
	private static FileWriter visitWriter;
	private static int cnt;

	
	
	
	public MyCrawler() throws Exception {
		 urlWriter = new FileWriter("urls_latimes.csv");
		 fetchWriter = new FileWriter("fetch_latimes.csv");
		 visitWriter = new FileWriter("visit_latimes.csv");
		 fetchWriter.append("URL,status code\n");
		 visitWriter.append("URL,size bytes,# of outlinks,content-type\n");
		 cnt = 0;
	}
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|xml|rss|json|woff2|webmanifest|mp3|mp4|zip|gz))(\\?.*$|$)");
			 /**
			 * This method receives two parameters. The first parameter is the page
			 * in which we have discovered this new url and the second parameter is
			 * the new url. You should implement this function to specify whether
			 * the given url should be crawled or not (based on your crawling logic).
			 * In this example, we are instructing the crawler to ignore urls that
			 * have css, js, git, ... extensions and to only accept urls that start
			 * with "http://www.viterbi.usc.edu/". In this case, we didn't need the
			 * referringPage parameter to make the decision.
			 */
			 @Override
			
	public boolean shouldVisit(Page referringPage, WebURL url) {
				 String href = url.getURL().toLowerCase();
				 boolean ret = !FILTERS.matcher(href).matches() && (href.startsWith("https://www.latimes.com/") || href.startsWith("http://www.latimes.com/"));
				 try {
					 urlWriter.append(url.toString().replace(',', '_'));
					 urlWriter.append(",");
					 if(ret) {
						 urlWriter.append("OK");
					 }
					 else {
						 urlWriter.append("N_OK");
					 }
					 urlWriter.append("\n");
					 urlWriter.flush();
				 }
				 catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				 }
				 return ret;
	}
			 @Override
	public void visit(Page page) {
				 String url = page.getWebURL().getURL();
				 System.out.println("URL: " + url);
				 int outLinksNum = 0;
				 cnt++;
				 if (page.getParseData() instanceof HtmlParseData) {
					 HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
					 String text = htmlParseData.getText();
					 String html = htmlParseData.getHtml();
					 Set<WebURL> links = htmlParseData.getOutgoingUrls();
					 outLinksNum += links.size();
					 System.out.println("Text length: " + text.length());
					 System.out.println("Html length: " + html.length());
					 System.out.println("Number of outgoing links: " + links.size());
				 }
				 try {
					 	String contentType = page.getContentType();
						if(contentType.indexOf(';') > 0)contentType = contentType.substring(0,contentType.indexOf(';'));
					 	visitWriter.append(url.replace(',','_') + "," + String.valueOf(page.getContentData().length) + "," + String.valueOf(outLinksNum) + "," + contentType + "\n");
						visitWriter.flush();
				 } 
				 catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				 }
				 
	}
			 @Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
				 String url = webUrl.getURL();
				 try {
					fetchWriter.append(url.replace(',','_') + "," + String.valueOf(statusCode) + "\n");
					fetchWriter.flush();
				 } 
				 catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				 }	
	}
	@Override
	public void onBeforeExit() {
		super.onBeforeExit();
		try {
			urlWriter.close();
			fetchWriter.close();
			visitWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        System.out.println("onBeforeExit");
        System.out.println(cnt);
        
	}
	
			
}
