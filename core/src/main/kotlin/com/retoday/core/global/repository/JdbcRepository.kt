package com.retoday.core.global.repository

import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface JdbcRepository<T, ID> : ListCrudRepository<T, ID>
