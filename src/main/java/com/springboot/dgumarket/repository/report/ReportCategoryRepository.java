package com.springboot.dgumarket.repository.report;


import com.springboot.dgumarket.model.report.ReportCategory;
import org.springframework.data.jpa.repository.JpaRepository;



public interface ReportCategoryRepository extends JpaRepository<ReportCategory, Integer> {

    ReportCategory findById(int id);
}
