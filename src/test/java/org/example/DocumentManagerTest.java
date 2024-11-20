package org.example;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class DocumentManagerTest {
  private DocumentManager documentManager;

  @Before
  public void setUp() {
    documentManager = new DocumentManager();
  }

  @Test
  public void testSaveWithNewDocument() {
    DocumentManager.Author author = DocumentManager.Author.builder()
            .id("author-1")
            .name("John Doe")
            .build();

    DocumentManager.Document document = DocumentManager.Document.builder()
            .title("Sample Title")
            .content("Sample Content")
            .author(author)
            .created(Instant.now())
            .build();

    DocumentManager.Document savedDocument = documentManager.save(document);

    assertNotNull("Document ID should be generated", savedDocument.getId());
    assertEquals("Titles should match", document.getTitle(), savedDocument.getTitle());
    assertEquals("Content should match", document.getContent(), savedDocument.getContent());
  }

  @Test
  public void testSaveWithExistingDocument() {
    DocumentManager.Document document = DocumentManager.Document.builder()
            .id("existing-id")
            .title("DOOOOOOc")
            .content("SMTH")
            .author(null)
            .created(Instant.now())
            .build();

    DocumentManager.Document savedDocument = documentManager.save(document);

    assertEquals("Document ID should not change", "existing-id", savedDocument.getId());
    assertEquals("Titles should match", document.getTitle(), savedDocument.getTitle());
  }

  @Test
  public void testFindById() {
    DocumentManager.Document document = DocumentManager.Document.builder()
            .id("doc-123")
            .title("Find Me")
            .content("Find me by ID")
            .author(null)
            .created(Instant.now())
            .build();

    documentManager.save(document);

    Optional<DocumentManager.Document> foundDocument = documentManager.findById("doc-123");
    assertTrue("Document should be found", foundDocument.isPresent());
    assertEquals("Titles should match", "Find Me", foundDocument.get().getTitle());
  }

  @Test
  public void testFindByIdNotFound() {
    Optional<DocumentManager.Document> foundDocument = documentManager.findById("nonexistent-id");
    assertFalse("Document should not be found", foundDocument.isPresent());
  }

  @Test
  public void testSearchByTitlePrefix() {
    documentManager.save(createSampleDocument("1", "PrefixMatch", "Some Content", "author-1"));
    documentManager.save(createSampleDocument("2", "NoMatch", "Other Content", "author-2"));

    DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
            .titlePrefixes(List.of("Prefix"))
            .build();

    List<DocumentManager.Document> results = documentManager.search(request);

    assertEquals("Should find one document", 1, results.size());
    assertEquals("Document ID should match", "1", results.get(0).getId());
  }

  @Test
  public void testSearchByContent() {
    documentManager.save(createSampleDocument("1", "Doc1", "ahahahhaha keyword hahahahah", "author-1"));
    documentManager.save(createSampleDocument("2", "Doc2", "cat cat cat", "author-2"));

    DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
            .containsContents(List.of("keyword"))
            .build();

    List<DocumentManager.Document> results = documentManager.search(request);

    assertEquals("Should find one document", 1, results.size());
    assertEquals("Document ID should match", "1", results.get(0).getId());
  }

  @Test
  public void testSearchByAuthorId() {
    documentManager.save(createSampleDocument("1", "Doc1", "Content 1", "author-1"));
    documentManager.save(createSampleDocument("2", "Doc2", "Content 2", "author-2"));

    DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
            .authorIds(List.of("author-1"))
            .build();

    List<DocumentManager.Document> results = documentManager.search(request);

    assertEquals("Should find one document", 1, results.size());
    assertEquals("Document ID should match", "1", results.get(0).getId());
  }

  @Test
  public void testSearchByCreatedDateRange() {
    Instant now = Instant.now();
    documentManager.save(createSampleDocument("1", "Doc1", "Content 1", "author-1", now.minusSeconds(100)));
    documentManager.save(createSampleDocument("2", "Doc2", "Content 2", "author-2", now.plusSeconds(3600)));

    DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
            .createdFrom(now.minusSeconds(1800))
            .createdTo(now.plusSeconds(1800))
            .build();

    List<DocumentManager.Document> results = documentManager.search(request);

    assertEquals("Should find one document", 1, results.size());
    assertEquals("Document ID should match", "1", results.get(0).getId());
  }

  private DocumentManager.Document createSampleDocument(String id, String title, String content, String authorId) {
    return createSampleDocument(id, title, content, authorId, Instant.now());
  }

  private DocumentManager.Document createSampleDocument(String id, String title, String content, String authorId, Instant created) {
    DocumentManager.Author author = DocumentManager.Author.builder()
            .id(authorId)
            .name("Author " + authorId)
            .build();

    return DocumentManager.Document.builder()
            .id(id)
            .title(title)
            .content(content)
            .author(author)
            .created(created)
            .build();
  }
}
