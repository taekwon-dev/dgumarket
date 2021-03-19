package com.springboot.dgumarket.repository.report;

import com.springboot.dgumarket.model.report.Report;
import com.springboot.dgumarket.model.report.ReportCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportCategoryRepository extends JpaRepository<ReportCategory, Integer> {

    ReportCategory findById(int id);
}
