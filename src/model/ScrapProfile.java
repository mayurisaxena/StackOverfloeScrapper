package model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScrapProfile {

   private String name;
   private String email;
   private String mobileNumber;
   private String publicProfileUrl;
   private String stackoverflowPicUrl;
   private String location;
   private String gender;
   private String facebookUrl;
   private String twitterUrl;
   private String githubUrl;
   private String profileFrom;
   private String cvLinkStackOverflow;
   private Set<String> userSiteUrl;
   private List<ScrapExperience> experiences;
   private Set<String> groups;
   private List<ScrapProject> projects;
   private List<ScrapPublication> publications;
   private List<ScrapLanguage> languages;
   private List<ScrapCourse> courses;
   private List<ScrapCertificate> certificates;
   private Map<String, String> skills;
   private List<ScrapEducation> education;
   private List<ScrapOpenSource> openSourceProjects;
   private List<ScrapTopPost> topPosts;

   

   public List<ScrapTopPost> getTopPosts() {
      return topPosts;
   }

   public void setTopPosts(List<ScrapTopPost> topPosts) {
      this.topPosts = topPosts;
   }

   public List<ScrapOpenSource> getOpenSourceProjects() {
      return openSourceProjects;
   }

   public void setOpenSourceProjects(List<ScrapOpenSource> openSourceProjects) {
      this.openSourceProjects = openSourceProjects;
   }

   public String getCvLinkStackOverflow() {
      return cvLinkStackOverflow;
   }

   public void setCvLinkStackOverflow(String cvLinkStackOverflow) {
      this.cvLinkStackOverflow = cvLinkStackOverflow;
   }

   public Set<String> getUserSiteUrl() {
      return userSiteUrl;
   }

   public void setUserSiteUrl(Set<String> userSiteUrl) {
      this.userSiteUrl = userSiteUrl;
   }

   public String getGithubUrl() {
      return githubUrl;
   }

   public void setGithubUrl(String githubUrl) {
      this.githubUrl = githubUrl;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List<ScrapEducation> getEducation() {
      return education;
   }

   public void setEducation(List<ScrapEducation> education) {
      this.education = education;
   }

   public String getLocation() {
      return location;
   }

   public void setLocation(String location) {
      this.location = location;
   }

   public List<ScrapExperience> getExperiences() {
      return experiences;
   }

   public void setExperiences(List<ScrapExperience> experiences) {
      this.experiences = experiences;
   }

   public Map<String, String> getSkills() {
      return skills;
   }

   public void setSkills(Map<String, String> skills) {
      this.skills = skills;
   }

   public Set<String> getGroups() {
      return groups;
   }

   public void setGroups(Set<String> groups) {
      this.groups = groups;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getMobileNumber() {
      return mobileNumber;
   }

   public void setMobileNumber(String mobileNumber) {
      this.mobileNumber = mobileNumber;
   }

   public String getPublicProfileUrl() {
      return publicProfileUrl;
   }

   public void setPublicProfileUrl(String publicProfileUrl) {
      this.publicProfileUrl = publicProfileUrl;
   }

   public String getStackoverflowPicUrl() {
      return stackoverflowPicUrl;
   }

   public void setStackoverflowPicUrl(String stackoverflowPicUrl) {
      this.stackoverflowPicUrl = stackoverflowPicUrl;
   }

   public String getGender() {
      return gender;
   }

   public void setGender(String gender) {
      this.gender = gender;
   }

   public String getFacebookUrl() {
      return facebookUrl;
   }

   public void setFacebookUrl(String facebookUrl) {
      this.facebookUrl = facebookUrl;
   }

   public String getTwitterUrl() {
      return twitterUrl;
   }

   public void setTwitterUrl(String twitterUrl) {
      this.twitterUrl = twitterUrl;
   }

   public String getProfileFrom() {
      return profileFrom;
   }

   public void setProfileFrom(String profileFrom) {
      this.profileFrom = profileFrom;
   }

   public List<ScrapProject> getProjects() {
      return projects;
   }

   public void setProjects(List<ScrapProject> projects) {
      this.projects = projects;
   }

   public List<ScrapPublication> getPublications() {
      return publications;
   }

   public void setPublications(List<ScrapPublication> publications) {
      this.publications = publications;
   }

   public List<ScrapLanguage> getLanguages() {
      return languages;
   }

   public void setLanguages(List<ScrapLanguage> languages) {
      this.languages = languages;
   }

   public List<ScrapCourse> getCourses() {
      return courses;
   }

   public void setCourses(List<ScrapCourse> courses) {
      this.courses = courses;
   }

   public List<ScrapCertificate> getCertificates() {
      return certificates;
   }

   public void setCertificates(List<ScrapCertificate> certificates) {
      this.certificates = certificates;
   }

}
