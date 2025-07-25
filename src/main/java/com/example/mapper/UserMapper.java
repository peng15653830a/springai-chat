package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    
    void insert(User user);
    
    User selectById(@Param("id") Long id);
    
    User selectByUsername(@Param("username") String username);
    
    void updateById(User user);
    
    void deleteById(@Param("id") Long id);
}