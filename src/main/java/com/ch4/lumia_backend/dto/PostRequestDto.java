	// src/main/java/com/ch4/lumia_backend/dto/PostRequestDto.java
	
	package com.ch4.lumia_backend.dto;
	
	import lombok.Getter;
	import lombok.Setter;
	
	@Getter
	@Setter
	public class PostRequestDto {
	    private String category; // 👈 글머리
	    private String title;
	    private String content;
	}
