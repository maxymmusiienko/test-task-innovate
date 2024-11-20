package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
  private final Map<String, Document> documents;

  public DocumentManager() {
    this.documents = new HashMap<>();
  }

  /**
   * Implementation of this method should upsert the document to your storage
   * And generate unique id if it does not exist, don't change [created] field
   *
   * @param document - document content and author data
   * @return saved document
   */
  public Document save(Document document) {
    if (document.getId() == null) {
      String uniqueID = UUID.randomUUID().toString();
      document.setId(uniqueID);
    }
    documents.put(document.getId(), document);
    return document;
  }

  /**
   * Implementation this method should find documents which match with request
   *
   * @param request - search request, each field could be null
   * @return list matched documents
   */
  public List<Document> search(SearchRequest request) {
    return documents.values().stream()
            .filter(document -> matchTitlePrefixes(request.getTitlePrefixes(), document.getTitle()))
            .filter(document -> matchContainsContents(request.getContainsContents(), document.getContent()))
            .filter(document -> matchAuthorIds(request.getAuthorIds(), document.getAuthor()))
            .filter(document -> matchCreatedRange(request.getCreatedFrom(), request.getCreatedTo(), document.getCreated()))
            .collect(Collectors.toList());
  }

  private boolean matchTitlePrefixes(List<String> titlePrefixes, String title) {
    if (titlePrefixes == null || titlePrefixes.isEmpty()) {
      return true;
    }
    return titlePrefixes.stream().anyMatch(prefix -> title != null && title.startsWith(prefix));
  }

  private boolean matchContainsContents(List<String> containsContents, String content) {
    if (containsContents == null || containsContents.isEmpty()) {
      return true;
    }
    return containsContents.stream().anyMatch(keyword -> content != null && content.contains(keyword));
  }

  private boolean matchAuthorIds(List<String> authorIds, Author author) {
    if (authorIds == null || authorIds.isEmpty()) {
      return true;
    }
    return author != null && authorIds.contains(author.getId());
  }

  private boolean matchCreatedRange(Instant createdFrom, Instant createdTo, Instant created) {
    if (created == null) {
      return false;
    }
    if (createdFrom != null && created.isBefore(createdFrom)) {
      return false;
    }
    return createdTo == null || !created.isAfter(createdTo);
  }

  /**
   * Implementation this method should find document by id
   *
   * @param id - document id
   * @return optional document
   */
  public Optional<Document> findById(String id) {
    Document document = documents.get(id);
    return Optional.ofNullable(document);
  }

  @Data
  @Builder
  public static class SearchRequest {
    private List<String> titlePrefixes;
    private List<String> containsContents;
    private List<String> authorIds;
    private Instant createdFrom;
    private Instant createdTo;
  }

  @Data
  @Builder
  public static class Document {
    private String id;
    private String title;
    private String content;
    private Author author;
    private Instant created;
  }

  @Data
  @Builder
  public static class Author {
    private String id;
    private String name;
  }
}
