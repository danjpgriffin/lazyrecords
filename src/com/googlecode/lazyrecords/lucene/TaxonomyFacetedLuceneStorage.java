package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.lucene.mappings.DelegatingStorage;
import com.googlecode.totallylazy.functions.Function2;
import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.functions.Lazy;
import com.googlecode.totallylazy.Sequence;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;

import java.io.IOException;

import static com.googlecode.totallylazy.Sequences.sequence;

public class TaxonomyFacetedLuceneStorage extends DelegatingStorage implements FacetedLuceneStorage {

    private final Lazy<DirectoryTaxonomyWriter> lazyTaxonomyWriter;
    private final FacetsConfig facetsConfig;
    private final FieldBasedFacetingPolicy facetingPolicy;

    public TaxonomyFacetedLuceneStorage(final LuceneStorage storage, final Directory directory, final FacetsConfig facetsConfig, FieldBasedFacetingPolicy facetingPolicy) {
        super(storage);
        this.lazyTaxonomyWriter = new Lazy<DirectoryTaxonomyWriter>() {
            @Override
            protected DirectoryTaxonomyWriter get() throws Exception {
                return new DirectoryTaxonomyWriter(directory);
            }
        };

        this.facetsConfig = facetsConfig;
        this.facetingPolicy = facetingPolicy;
    }

    @Override
    public Number add(Sequence<Document> documents) throws IOException {
        final Sequence<Document> facetedDocuments = documents.map(document -> {
            final Document facetedDocument = sequence(document.getFields()).fold(new Document(), withFacetFields());
            final DirectoryTaxonomyWriter taxonomyWriter = Callers.call(lazyTaxonomyWriter);
            return facetsConfig.build(taxonomyWriter, facetedDocument);
        }).realise();

        return storage.add(facetedDocuments);
    }

    @Override
    public void flush() throws IOException {
        Callers.call(lazyTaxonomyWriter).commit();
        storage.flush();
    }

    @Override
    public void close() throws IOException {
        Closeables.close(Callers.call(lazyTaxonomyWriter));
        storage.close();
    }

    private Function2<Document, IndexableField, Document> withFacetFields() {
        return (document, indexableField) -> {
            if (facetingPolicy.value().matches(indexableField.name()) && !indexableField.stringValue().isEmpty()) {
                document.add(new FacetField(indexableField.name(), indexableField.stringValue()));
            }
            document.add(indexableField);
            return document;
        };
    }

    @Override
    public TaxonomyReader taxonomyReader() throws IOException {
        return new DirectoryTaxonomyReader(Callers.call(lazyTaxonomyWriter));
    }

    @Override
    public FacetsConfig facetsConfig() {
        return facetsConfig;
    }
}
