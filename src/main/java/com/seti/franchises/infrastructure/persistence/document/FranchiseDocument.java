package com.seti.franchises.infrastructure.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB root document for a franchise.
 * Collection: franchises. Embeds branches and products (denormalized model).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "franchises")
public class FranchiseDocument {

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("branches")
    @Builder.Default
    private List<BranchDocument> branches = new ArrayList<>();
}
