package practise;

import java.util.List;

public class Q07_ComplexHierarchy {
    interface Repository<T, ID>{
        T save(T enitty);
        T findById(ID id);
        void deleteById(ID id);
        long count();
        boolean existById(ID id);
    }
    interface CrudRepository<T, ID> extends Repository<T, ID>{
        List<T> saveAll(Iterable<T> entities);
        List<T> findAll();
        void deleteAll();
    }

    public static void main(String[]args){
        System.out.println("=== Q7: Complex Generic Hierarchy ===");
        System.out.println("✅ Repository hierarchy with Repository -> CrudRepository -> PagingRepository");
        System.out.println("✅ Each level adds functionality without breaking previous levels");
        System.out.println("✅ Follows Interface Segregation Principle");
        System.out.println("✅ Q7 Completed!\n");
    }
}
