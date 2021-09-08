package com.example.user.marina;

public class Entity {

    String entity;
    String type;

    public Entity(String entity, String type)
    {
        this.entity = entity;
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public String getEntity()
    {
        return entity;
    }
}