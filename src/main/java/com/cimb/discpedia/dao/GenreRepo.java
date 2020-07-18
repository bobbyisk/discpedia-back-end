package com.cimb.discpedia.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cimb.discpedia.entity.Genre;

public interface GenreRepo extends JpaRepository<Genre, Integer>{

}
