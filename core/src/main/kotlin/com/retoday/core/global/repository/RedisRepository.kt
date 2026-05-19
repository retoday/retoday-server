package com.retoday.core.global.repository

import org.springframework.data.keyvalue.repository.KeyValueRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface RedisRepository<T, ID> : KeyValueRepository<T, ID>
