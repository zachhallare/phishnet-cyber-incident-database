package dao;

import model.AttackType;
import java.sql.SQLException;
import java.util.List;

public interface AttackTypeDAO {
    AttackType findById(int attackTypeID) throws SQLException;
    AttackType findByName(String attackName) throws SQLException;
    List<AttackType> findAll() throws SQLException;
    boolean create(AttackType attackType) throws SQLException;
    boolean update(AttackType attackType) throws SQLException;
    boolean delete(int attackTypeID) throws SQLException;
}