package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import model.ScrapCertificate;
import model.ScrapEducation;
import model.ScrapExperience;
import model.ScrapOpenSource;
import model.ScrapProfile;
import model.ScrapTopPost;

public class StackOverflowScrape {
  
   private static final org.apache.log4j.Logger logger = Logger.getLogger(StackOverflowScrape.class);
   static ObjectMapper mapper = new ObjectMapper();
   
   static File trackUrl;
   static String path;
   static Properties prop;
   static String temppath;
   static File startUrlPath;
   
   public StackOverflowScrape () {
      logger.info("In StackOverflowScrape()");
      prop = new Properties();
      InputStream input = null;
      try {
      String filename = "config.properties";
      input = getClass().getClassLoader().getResourceAsStream(filename);;
      prop.load(input);
      path = prop.getProperty("path");
      temppath = prop.getProperty("temppath");
      trackUrl = new File(path + "trackUrl.txt");
      if (!trackUrl.exists()) {
         trackUrl.createNewFile();
      }
      startUrlPath = new File(path+"startUrl.txt");
      if (!startUrlPath.exists()) {
         startUrlPath.createNewFile();
      }
      
      logger.info("path :"+path);
      logger.info("temppath: "+temppath);
      logger.info("trackUrl: "+trackUrl);
      logger.info("startUrlPatj: "+startUrlPath);
      } catch(Exception e) {
         logger.error("Initialising parameters: ",e);
      } finally {
         try {
            input.close();
         } catch (IOException e) {
            logger.error("Files initialisation failed", e);
         }
      }
   }
   
   public static void main (String args[]) {
      try {
         logger.info("In main...");
         String url = new String("http://stackoverflow.com/users?tab=Reputation&filter=all&page=1");
         StackOverflowScrape obj = new StackOverflowScrape();
         BufferedReader br = new BufferedReader(new FileReader(startUrlPath));
         String line;
         if (br.ready()) {
            logger.info("Reading from file for start url");
         }
         while ((line = br.readLine()) != null) {
            if(line.contains("users")) {
               url = line;
            }
         }
         scrapeProfileUrls(url);
      } catch (Throwable e) {
         logger.error("In main",e);
      }
   }

   public static void scrapeProfileUrls(String url) {
      try {
         logger.info("In scrapeProfileUrls for url : " +url);
         Document doc = null;
         doc = Client.stackOverflowProxyCall(url);
         
         Elements listEle = doc.select(".user-details a");
         Iterator<Element> itrElement = listEle.iterator();
         if(!itrElement.hasNext()){
            Thread.sleep(30*1000);
            scrapeProfileUrls(url);
         }
         while (itrElement.hasNext()) {
            ScrapProfile profile = new ScrapProfile();
            String link = "http://stackoverflow.com/"+itrElement.next().attr("href");
            if (!checkUrlALreadyScraped(link)) {
               profile.setPublicProfileUrl(link);
               profile = getStackOverflowProfile(profile);
               if (profile.getProfileFrom() != null) {
                  logger.info("Profile found for " + profile.getName());
                  writeProfileToFile(profile);
               }
            }
         }
         writeScrapedUrl(url);
         Element nextElement = doc.select(".next").parents().get(0);
         String nextlink = "http://stackoverflow.com/"+ nextElement.attr("href");
         
         BufferedWriter bw = null;
         try {
            bw = new BufferedWriter(new FileWriter(startUrlPath,true));
            bw.append(nextlink);
            bw.newLine();
            bw.close();
         } catch (IOException e) {
            logger.error("Writing starturl : " + nextlink, e);
         }
         scrapeProfileUrls(nextlink);
      } catch (Exception e) {
         logger.error(e,e);
      }
      
   }
   
   public static ScrapProfile getStackOverflowProfile(ScrapProfile profile) {
      try {
         logger.info("In getStackOverflowProfile for url : " + profile.getPublicProfileUrl());
         Document doc = null;
         doc = Client.stackOverflowProxyCall(profile.getPublicProfileUrl());
         Elements cvCheck = doc.select(".btn-careers");
         
         String name = doc.select(".user-card-name").text();
         if (name != null && !name.equals("")){
            logger.info("Profile found for url: "+profile.getPublicProfileUrl());
            profile.setProfileFrom("SCRAPPED_ONLY");
         } else {
            logger.info("Thread sleep for url : "+profile.getPublicProfileUrl());
            Thread.sleep(30*1000);
            getStackOverflowProfile(profile);
         }
         name = doc.select(".user-card-name").text();
         name = name.replace(doc.select(".top-badge").text(), "");  
         profile.setName(name);
         
         String picUrl = doc.select(".avatar-user").attr("href");
         profile.setStackoverflowPicUrl(picUrl);
         
         Elements eleLocation = doc.select(".icon-location").parents();
         if (eleLocation.size()>=1)
            profile.setLocation(eleLocation.get(0).text());
         
         Elements eleTwitter = doc.select(".icon-twitter").parents();
         if (eleTwitter.size()>=1)   
            profile.setTwitterUrl(eleTwitter.get(0).child(1).attr("href"));
         
         Elements eleGithub = doc.select(".icon-github").parents();
         if (eleGithub.size()>=1)  
            profile.setGithubUrl(eleGithub.get(0).child(1).attr("href"));
         
         Elements eleSite = doc.select(".icon-site").parents();
         Set<String> userSiteUrl = new HashSet<String>();
         if (profile.getUserSiteUrl() != null) {
            userSiteUrl.addAll(profile.getUserSiteUrl());
         }
         if (eleSite.size()>=1) {
            userSiteUrl.add(eleSite.get(0).child(1).attr("href"));
            profile.setUserSiteUrl(userSiteUrl);
         }
         
         // Scrape current experience when company present
         List<ScrapExperience> experiences = new ArrayList<ScrapExperience>();
         try {
         String emp = doc.select(".current-position").text();
         String[] emps = emp.split(" at ");
         if (emps.length>1) {
            ScrapExperience experience = new ScrapExperience();
            experience.setDescription(emps[0]);
            experience.setOrganisationName(emps[1]);
            experiences.add(experience);
            profile.setExperiences(experiences);
         }
         } catch (Exception e) {
            logger.error("Scrape experience failed for profile : "+profile.getPublicProfileUrl(),e);
         }
         
         // Scrape top skills 
         Map<String,String> mapSkills = new HashMap<String,String>();
         try {
         Elements eleSkillsRow = doc.select(".tag-container");
         Iterator<Element> itrEleSkillsRow = eleSkillsRow.iterator();
         while(itrEleSkillsRow.hasNext()) {
            Element skillRow = (Element) itrEleSkillsRow.next();
            Elements skillsColumn = skillRow.select(".col");
            Iterator<Element> itrSkillsColumn = skillsColumn.iterator();
            while(itrSkillsColumn.hasNext()) {
               Element skillColumn = itrSkillsColumn.next();
               String skillName = skillColumn.select(".post-tag").text();
               String score = skillColumn.select(".stat .row .number").get(0).text().replace("Score", "");
               mapSkills.put(skillName, score);
            }
         }
         } catch (Exception e) {
            logger.error("Scrape skills failed for profile : "+profile.getPublicProfileUrl(),e);
         }
         profile.setSkills(mapSkills);
         
         // Find email from description
         String email = null;
         String description = doc.select(".bio").text();
         String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
         Pattern pattern = Pattern.compile(regex);
         String[] descriptionArr = description.split(" ");
         for(String str:descriptionArr) {
            Matcher matcher = pattern.matcher(str);
            if(matcher.matches()) {
               email = matcher.group(0);
               logger.info("Email found for url : "+profile.getPublicProfileUrl() + " email : "+email);
            }
         }
         profile.setEmail(email);
         
         
         //Scrape top posts
         List<ScrapTopPost> listPosts = new ArrayList<ScrapTopPost>();
         try {
         Elements elePosts = doc.select("#top-posts .post-container");
         Iterator<Element> itrPosts = elePosts.iterator();
         while (itrPosts.hasNext()) {
            Element posts = itrPosts.next();
            ScrapTopPost post = new ScrapTopPost();
            post.setVoteCount(posts.select(".vote").text());
            post.setQuestion(posts.select(".answer-hyperlink").text());
            post.setUrl("http://stackoverflow.com/"+posts.select(".answer-hyperlink").attr("href"));
            post.setDate(posts.select(".relativetime").text());
            listPosts.add(post);
         }
         } catch (Exception e) {
            logger.error("Scrape top posts failed for profile : "+profile.getPublicProfileUrl(),e);
         }
         profile.setTopPosts(listPosts);
         try {
         if (!cvCheck.isEmpty() && cvCheck.size()>0) {
            profile.setCvLinkStackOverflow("http://stackoverflow.com/"+cvCheck.attr("href"));
            profile = getStackOverflowCvProfile(profile);
         }
         } catch (Exception e) {
            logger.error("Scrape CV failed for profile : "+profile.getPublicProfileUrl(),e);
         }
         
      } catch (Exception e) {
         logger.error(e,e);
      }
      return profile;
   }

   public static ScrapProfile getStackOverflowCvProfile(ScrapProfile profile) {
      try {
         Document doc = null;
         doc = Client.stackOverflowProxyCall(profile.getCvLinkStackOverflow());

         // Scrape first block
         Element eleDisplay = doc.select("#section-personal").get(0);
         String name = eleDisplay.select("h1").text();
         
         if (name != null && !name.equals("")){
            profile.setProfileFrom("SCRAPPED_ONLY");
         } else {
            logger.info("Thread sleep for url : "+profile.getPublicProfileUrl());
            Thread.sleep(30*1000);
            getStackOverflowCvProfile(profile);
         }
         
         profile.setName(name);
         
         String picUrl = doc.select(".gravatar").attr("href");
         profile.setStackoverflowPicUrl(picUrl);
         
         Elements eleThird = eleDisplay.select("#user-meta li");
         String location = eleThird.get(0).text();
         profile.setLocation(location);
         String userSiteUrl = eleThird.select("#website").text();
         Set<String> userSiteUrls = new HashSet<String>();
         if (profile.getUserSiteUrl() != null) {
            userSiteUrls.addAll(profile.getUserSiteUrl());
         }
         userSiteUrls.add(userSiteUrl);
         profile.setUserSiteUrl(userSiteUrls);
         String twitter = eleThird.select(".twitter").attr("href");
         profile.setTwitterUrl(twitter);

         // Scrape email from statement
         String email = null;
         try {
            String statement = doc.select("#statement").text();
            String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
            Pattern pattern = Pattern.compile(regex);
            String[] descriptionArr = statement.split(" ");
            for (String str : descriptionArr) {
               Matcher matcher = pattern.matcher(str);
               if (matcher.matches()) {
                  email = matcher.group(0);
                  logger.info("Email found for url : " + profile.getPublicProfileUrl() + " email : " + email);
               }
            }
         } catch (Exception e) {
            logger.error("Scrape email failed for Cv : "+profile.getPublicProfileUrl(),e);
         }
         profile.setEmail(email);

         // Scrape Skills
         Map<String, String> mapSkillScore = new HashMap<String, String>();
         try {
         Elements eleSkills = doc.select(".post-tag");
         Iterator<Element> itrSkills = eleSkills.iterator();
         while (itrSkills.hasNext()) {
            String skillName = itrSkills.next().text();
            mapSkillScore.put(skillName, "");
         }
         profile.setSkills(mapSkillScore);
         } catch (Exception e) {
            logger.error("Scrape Skills failed for Cv : "+profile.getPublicProfileUrl(),e);
         }
         
         // Scrape Experience
         List<ScrapExperience> listExp = new ArrayList<ScrapExperience>();
         try {
         Elements eleExperience = doc.select("#cv-experience .cv-section");
         Iterator<Element> itrExp = eleExperience.iterator();
         while (itrExp.hasNext()) {
            ScrapExperience experience = new ScrapExperience();
            Element exp = itrExp.next();
            String company = exp.select(".location").text();
            experience.setOrganisationName(company);
            String desig = exp.select(".h4").text();
            desig = desig.replace(company, "");
            experience.setDesignation(desig);
            String duration = exp.select(".time-frame").text();
            String[] durationR = duration.split("–");
            String startDate = durationR[0].trim();
            String endDate = durationR.length>1?durationR[1]:null;
            experience.setStartDate(startDate);
            experience.setEndDate(endDate);
            String description = exp.select(".description").text();
            experience.setDescription(description);

            Elements eleExpSkills = exp.select(".tags .post-tag");
            Iterator<Element> itrExpSkills = eleExpSkills.iterator();
            while (itrExpSkills.hasNext()) {
               String skillName = itrExpSkills.next().text();
               mapSkillScore.put(skillName, "");
            }
            profile.setSkills(mapSkillScore);
            listExp.add(experience);
         }
         } catch (Exception e) {
            logger.error("Scrape experience failed for Cv : "+profile.getPublicProfileUrl(),e);
         }
         profile.setExperiences(listExp);

         // Scrape Education
         List<ScrapEducation> listEdu = new ArrayList<ScrapEducation>();
         try {
         Elements eleEdu = doc.select("#cv-education .cv-section");
         Iterator<Element> itrEdu = eleEdu.iterator();
         while (itrEdu.hasNext()) {
            ScrapEducation education = new ScrapEducation();
            Element edu = itrEdu.next();
            String universityName = edu.select(".location").text();
            String degree = edu.select(".h4").text();
            degree = degree.replace(universityName, "");
            String duration = edu.select(".time-frame").text();
            String[] durationR = duration.split("–");
            String startDate = durationR[0].trim();
            String endDate = null;
            if (durationR.length>1) {
               endDate = durationR[1].trim();
            }
            String description = edu.select(".description").text();

            education.setName(universityName);
            education.setDegree(degree);
            education.setFromDate(startDate);
            education.setEndDate(endDate);
            education.setDescription(description);
            listEdu.add(education);
         }
         } catch (Exception e) {
            logger.error("Scrape education failed for Cv : "+profile.getPublicProfileUrl(),e);
         }
         profile.setEducation(listEdu);

         //Scrape Certifications
         List<ScrapCertificate> listCert = new ArrayList<ScrapCertificate>();
         try {
         Elements eleCert = doc.select("#cv-certifications .cv-section");
         Iterator<Element> itrCert = eleCert.iterator();
         while(itrCert.hasNext()) {
            ScrapCertificate certificate = new ScrapCertificate();
            Element cert = itrCert.next();
            String title = cert.select(".h4").text();
            String certName = title.split("\\(")[0];
            String licensNo = null;
            if (title.split("\\(").length>1) {
               licensNo = title.split("\\(")[1].replace(")", "");
            }
            String startDate = cert.select(".time-frame").text();
            certificate.setName(certName);
            certificate.setLicenseNo(licensNo);
            certificate.setStartDate(startDate);
            listCert.add(certificate);
         }
         } catch (Exception e) {
            logger.error("Scrape certifications failed for Cv : "+profile.getPublicProfileUrl(),e);
         }
         profile.setCertificates(listCert);
         
         //Scrap OpenSource Projects
         List<ScrapOpenSource> listOpenSrc = new ArrayList<ScrapOpenSource>();
         try {
         Element eleOpenSrcBlock = doc.select("#open-source").get(0).nextElementSibling();
         Element eleOpenSource = eleOpenSrcBlock.select(".section-header").get(0).nextElementSibling();
         Elements eleProjects = eleOpenSource.select(".project");
         Iterator<Element> itrProjects = eleProjects.iterator();
         if (itrProjects.hasNext()) {
            logger.info("Open source projects found for url : "+profile.getCvLinkStackOverflow());
         }
         while (itrProjects.hasNext()) {
            ScrapOpenSource project = new ScrapOpenSource();
            Element prj = itrProjects.next();
            String projectName = prj.select(".h4 a").text();
            String projectUrl = prj.select(".h4 a").attr("href");
            String description = prj.select(".description p").text();
            String timeFrame = prj.select(".time-frame").text();
            String startDate = null;
            String endDate = null;
            String source = null;
            if (timeFrame != null && timeFrame.length()>0) {
               String duration = timeFrame.split(";")[0].split(",")[1];
               String[] durationR = duration.split("-");
               startDate = durationR[0].trim();
               endDate = durationR.length>1?durationR[1]:null;
               source = timeFrame.split(";")[0].split(",")[0];
            }
            project.setName(projectName);
            project.setLink(projectUrl);
            project.setStartDate(startDate);
            project.setEndDate(endDate);
            project.setSource(source);
            project.setDescription(description);
            listOpenSrc.add(project);
         }
         } catch (Exception e) {
            logger.error("Scrape OpenSource projects failed for Cv : "+profile.getPublicProfileUrl(),e);
         }
         profile.setOpenSourceProjects(listOpenSrc);
         
         
         
      } catch (Exception e) {
         logger.error(e, e);
      }
      return profile;
   }
   
   public static void writeScrapedUrl(String link) {
      logger.info("In writeScrapedUrl for url "+ link);
      BufferedWriter bw = null;
      try {
         bw = new BufferedWriter(new FileWriter(trackUrl, true));
         bw.append(link);
         bw.newLine();
         
      } catch (IOException e) {
         logger.error("In writeScrapedUrl for url : " + link, e);
      } finally {
         try {
            bw.close();
         } catch (IOException e) {
            logger.error("In finally for writeScrapedUrl for url : " + link, e);
         }
      }
   }
   
   public static boolean checkUrlALreadyScraped(String link) {
      logger.info("In checkUrlALreadyScraped for url :"+link);
      Path path1 = Paths.get(trackUrl.getAbsolutePath());
      try (Stream<String> filteredLines = Files.lines(path1)
            .filter(s -> s.contains(link))) {
         Optional<String> hasUrl = filteredLines.findFirst();
         if (hasUrl.isPresent()) {
            logger.info("Link already scraped " + link);
            return true;
         } else {
            logger.info("Link not scraped " + link);
            return false;
         }
      } catch (Exception e) {
         logger.error("checkUrlALreadyScraped: ",e);
         return false;
      }
   }
   
   public static void writeProfileToFile(ScrapProfile profile) {
      String link = profile.getPublicProfileUrl();
      logger.info("In writeProfileToFile for link :" +link);
      String[] str = link.split("/");
      String stackId = "/"+str[str.length-2]+"_"+str[str.length-1];
      try {
      DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Date date = new Date();
      String folderName = dateFormat.format(date);
      String finalPath = path + folderName;
      File folder = new File(finalPath);
      if (!folder.exists()) {
         folder.mkdirs();
      }
      
         File oldFile = new File(finalPath + stackId + ".json");
         if (oldFile.exists()) {
            File newFile = new File(temppath + stackId + ".json");

            mapper.writeValue(newFile, profile);

            if (newFile.length() > oldFile.length()) {
               mapper.writeValue(new File(finalPath + stackId + ".json"), profile);
            }
            newFile.delete();
         } else {
            mapper.writeValue(new File(finalPath + stackId + ".json"), profile);
         }
         writeScrapedUrl(profile.getPublicProfileUrl());
      } catch (Exception e) {
         logger.error("File write failed for profile : " + stackId, e);
      }
   }
   
}
