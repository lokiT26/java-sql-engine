# Java SQL Engine

![Language](https://img.shields.io/badge/Language-Java%2021-blue.svg)
![Build](https://img.shields.io/badge/Build-Maven-red.svg)
![Status](https://img.shields.io/badge/Status-In%20Development-orange.svg)

A foundational project to build a lightweight, relational database engine from scratch in Java. The primary goal is to gain a deep, systems-level understanding of database internals, including storage, indexing, concurrency, and recovery.

This repository also contains a detailed [developer log](./docs/devlog/) that chronicles the design decisions, challenges, and learning process throughout the project's development.

## 🏛️ Architecture Overview

The engine is designed with a classic, layered database architecture.

[//]: # (This diagram will be created once the core components are more mature.)
![Architecture Diagram](link_to_architecture_diagram.png)

`Client -> Parser -> Planner -> Executor -> Transaction & Storage Manager`

*   **SQL Parser:** Converts SQL strings into an Abstract Syntax Tree (using ANTLR).
*   **Query Planner:** Translates the AST into a logical plan of operators.
*   **Execution Engine:** Executes the plan (e.g., `SeqScan`, `IndexScan`).
*   **Transaction Manager:** Ensures ACID properties using locking and logging.
*   **Storage Manager:** Manages data on disk through a buffer pool.
    *   **Buffer Manager:** Caches disk pages in memory using an LRU policy.
    *   **Disk Manager:** Handles the physical I/O of reading/writing pages to table files.
*   **Index Manager:** Manages B+-Tree indexes for fast lookups.

## ✅ Project Roadmap

- [ ] **Phase 1: The Storage Layer**
    - [x] Initial Project Setup
    - [x] `Page` Class and On-Disk Representation
    - [ ] `DiskManager` for Raw Page I/O
    - [ ] `BufferPoolManager` with LRU Caching
    - [ ] Heap File and Tuple Storage
- [ ] **Phase 2: Basic Query Execution**
    - [ ] SQL Parser (ANTLR)
    - [ ] `SeqScan` (Sequential Scan) Executor
- [ ] **Phase 3: Indexing**
    - [ ] B+-Tree Implementation
    - [ ] `IndexScan` Executor
- [ ] **Phase 4: Concurrency & Recovery**
    - [ ] Transaction Manager
    - [ ] Write-Ahead Logging (WAL)
    - [ ] Strict Two-Phase Locking (2PL)

## 🚀 Getting Started

This project is built using **Java 21** and Maven.

```shell
# Clone the repository
git clone https://github.com/lokiT26/java-sql-engine.git

# Navigate into the project directory
cd java-sql-engine

# Build the project and run tests
mvn clean install