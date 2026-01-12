package com.sachet.parallel_asynchronous.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ReviewDto {
    @JsonProperty("rating")
    private double rating;
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("date")
    private Date date;
    @JsonProperty("reviewerName")
    private String reviewerName;
    @JsonProperty("reviewerEmail")
    private String reviewerEmail;
    @JsonProperty("productId")
    private Long productId;
}
