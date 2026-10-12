package com.yapi365.service;

public record StoredFile(String relativePath, long size, String sha256) {
}
