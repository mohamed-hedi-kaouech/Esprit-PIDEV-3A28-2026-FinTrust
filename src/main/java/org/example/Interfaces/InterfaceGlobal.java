package org.example.Interfaces;

import java.util.List;

public interface InterfaceGlobal<T> {
    void Add(T t);
    void Delete(Integer id);
    void Update(T t);
    List<T> ReadAll();
    T ReadId(Integer id);

}
