package questions;

import java.util.List;

interface Repository<T, ID>{
    void save(T data);
    T findById(ID id);
    List<T> findAll();
    void delete(ID id);
}

class UserRepository implements Repository{

    @Override
    public void save(Object data) {

    }

    @Override
    public Object findById(Object o) {
        return null;
    }

    @Override
    public List findAll() {
        return List.of();
    }

    @Override
    public void delete(Object o) {

    }
}

public class GenericInterface {
    public static void main(String[] args){
        System.out.println("generic interface");
    }
}
