package external;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class GitHubClient {
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	private static final String DEFAULT_KEYWORD = "developer";
	
	public List<Item> search(double lat, double lon, String keyword) {
		if(keyword == null) {  //如果用户没提供keyword，就用默认的值
			keyword = DEFAULT_KEYWORD;
		}
		
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String url = String.format(URL_TEMPLATE, keyword, lat, lon);//将template中的%s替换为输入值
		CloseableHttpClient httpClient = HttpClients.createDefault(); //创建后端接收请求的东西，实现底层实现
		
		try {
			CloseableHttpResponse response = httpClient.execute(new HttpGet(url)); //发送请求，得到response
			                                                      //get request，
			if (response.getStatusLine().getStatusCode() != 200) {
				return new ArrayList<>();
			}
			HttpEntity entity = response.getEntity();  //response body
			if (entity == null) {
				return new ArrayList<>();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            //BufferedReader默认每次读8kb的数据（可以修改每次读入的长度 ）        //entity.getContent() 返回流数据，直到end
			StringBuilder responseBody = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				         //每次读一行，读后自动跳转到下一行
				responseBody.append(line);
			}
			JSONArray array = new JSONArray(responseBody.toString());
			return getItemList(array);
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new ArrayList<>();
	}
	
	private List<Item> getItemList(JSONArray array) {
		List<Item> itemList = new ArrayList<>();
		List<String> descriptionList = new ArrayList<>();

		for (int i = 0; i < array.length(); ++i) {
			JSONObject object = array.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();
			
			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));
			
			// We need to extract categories from description since GitHub API
			// doesn't return keywords.
			if (object.getString("description").equals("\n")) {
				descriptionList.add(object.getString("title"));
			} else {
				descriptionList.add(object.getString("description"));
			}

			Item item = builder.build();
			itemList.add(item);
		}
		// We need to get keywords from multiple text in one request since
		// MonkeyLearnAPI has a limitation on request per minute.
		List<List<String>> keywords = new ArrayList<>();	
		String[] descriptionArray = descriptionList.toArray(new String[descriptionList.size()]); // Convert list to an array of the same type.
		try {
			 keywords = MonkeyLearnClient.extractKeywords(descriptionArray); // Call MonkeyLearn API.
		}
		catch(Exception e) {
			System.out.println("Keywords apis error");
		}
		for (int i = 0; i < keywords.size(); ++i) {
			List<String> list = keywords.get(i);
			// Why do we use HashSet but List here?
			Set<String> set = new HashSet<String>(list);
			itemList.get(i).setKeywords(set);
		}

		return itemList;
	}
	
	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		return obj.isNull(field) ? "" : obj.getString(field);
	}

	
	public static void main(String[] args) {
		GitHubClient githubClient = new GitHubClient();
		List<Item> list = githubClient.search(37.38, -122.08, null);
		for (Item item : list) {
			JSONObject jsonObject = item.toJSONObject();
			System.out.println(jsonObject);
		}
	}	
}
