-- MySQL dump 10.13  Distrib 9.1.0, for macos14 (arm64)
--
-- Host: localhost    Database: psms_db
-- ------------------------------------------------------
-- Server version	9.1.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `project_invitations`
--

DROP TABLE IF EXISTS `project_invitations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_invitations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `invited_by` bigint DEFAULT NULL,
  `role` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `token` varchar(255) NOT NULL,
  `project_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKh95a4adwexf6pv53eh7nfm8rf` (`token`),
  KEY `FKhk66j7po8n11yhiagqfvtpn0l` (`project_id`),
  KEY `idx_invitation_token` (`token`),
  KEY `idx_invitation_email_project` (`email`,`project_id`),
  KEY `idx_invitation_status` (`status`),
  CONSTRAINT `FKhk66j7po8n11yhiagqfvtpn0l` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_invitations`
--

LOCK TABLES `project_invitations` WRITE;
/*!40000 ALTER TABLE `project_invitations` DISABLE KEYS */;
INSERT INTO `project_invitations` VALUES (1,NULL,'niroshanb14@gmail.com','2025-11-15 05:39:00.409711',4,'MANAGER','REVOKED','20f119e9-d9c1-45a6-8a7d-75c136d526d7',2),(2,NULL,'niroshanb14@gmail.com','2025-11-15 05:47:23.234265',4,'MANAGER','ACCEPTED','638b40c9-25e5-4ddc-b479-06002c5a2e54',2),(3,'2025-11-08 07:25:42.941096','ndimuthu7@gmail.com','2025-11-15 07:25:42.937799',4,'MANAGER','ACCEPTED','78a45416-2126-4e6a-8e48-0d199125d5ac',2),(4,'2025-11-08 08:03:05.921729','ndimuthu7@gmail.com','2025-11-15 08:03:05.750954',4,'MANAGER','ACCEPTED','61a0327b-86ba-46c7-823b-f08c0ce437d9',2),(5,'2025-11-08 08:12:37.082047','niroshanb14@gmail.com','2025-11-15 08:12:37.075170',4,'CONTRIBUTOR','ACCEPTED','dff955b9-c8f8-4876-b826-c0694d173e13',3),(6,'2025-11-08 08:13:57.762831','ndimuthu7@gamil.com','2025-11-15 08:13:57.755476',4,'MANAGER','REVOKED','6fbe5f40-f4f2-400f-b46e-bd1cea65795e',3),(7,'2025-11-08 08:16:04.645895','ndimuthu7@gmail.com','2025-11-15 08:16:04.640774',4,'MANAGER','ACCEPTED','d446bffc-45f0-4e01-ae55-5a3da1dad90c',3),(8,'2025-11-08 08:18:49.569544','ndimuthu7@gmail.com','2025-11-15 08:18:49.568590',4,'MANAGER','ACCEPTED','2598d0e8-f334-4160-976e-79b12c1aa764',3),(9,'2025-11-08 08:23:26.979387','ndimuthu7@gmail.com','2025-11-15 08:23:26.973644',4,'MANAGER','ACCEPTED','2da0197d-9022-4566-913d-a43a4a6efd93',3),(10,'2025-11-08 08:24:29.535779','ndimuthu7@gmail.com','2025-11-15 08:24:29.534051',4,'MANAGER','ACCEPTED','86fa9972-fa5f-4125-a6d9-aa5b01fd0636',5),(11,'2025-11-08 08:29:15.866777','ndimuthu7@gmail.com','2025-11-15 08:29:15.863115',4,'MANAGER','ACCEPTED','41a5eee3-921d-48b0-9ceb-a52760cd7273',5),(12,'2025-11-08 08:32:37.328235','ndimuthu7@gmail.com','2025-11-15 08:32:37.305534',4,'MANAGER','ACCEPTED','e0a59969-19c1-48ae-a196-1b9024424a0e',5),(13,'2025-11-08 10:16:33.838807','niroshanb14@gmail.com','2025-11-15 10:16:33.832213',8,'MANAGER','ACCEPTED','8a2abb44-552c-4db1-b60f-a5871c739a2b',3),(14,'2025-11-08 10:33:44.853871','ndimuthu7@gmail.com','2025-11-15 10:33:44.849699',7,'MANAGER','ACCEPTED','e0140191-2c06-4d86-8532-cd05ba0390f8',3),(15,'2025-11-08 10:42:16.359078','ndimuthu7@gmail.com','2025-11-15 10:42:16.341017',7,'MANAGER','ACCEPTED','3e8cc0ce-c3b1-4fdd-9613-a352412c2ff2',3),(16,'2025-11-08 11:10:05.528825','ndimuthu7@gmail.com','2025-11-15 11:10:05.524602',4,'CONTRIBUTOR','ACCEPTED','034e8636-7949-4e7a-8629-058e62bb35d3',2),(17,'2025-11-08 11:36:37.685471','niroshanb14@gmail.com','2025-11-15 11:36:37.677511',4,'MANAGER','ACCEPTED','3f40285e-81a9-4563-b572-c11c5ab62029',5),(18,'2025-11-08 11:49:21.211469','ndimuthu7@gmail.com','2025-11-15 11:49:21.207181',4,'CONTRIBUTOR','ACCEPTED','eb516781-69b3-4472-a3f3-a5149ce6b862',5),(19,'2025-11-08 11:55:11.614611','ndimuthu7@gmail.com','2025-11-15 11:55:11.614131',7,'CONTRIBUTOR','ACCEPTED','4a47dd7d-5f84-4f42-8130-36487994366d',5),(20,'2025-11-08 12:00:18.936848','ndimuthu7@gmail.com','2025-11-15 12:00:18.933861',7,'MANAGER','ACCEPTED','6f16774c-5147-4049-881d-9f21ea6e9e34',5),(21,'2025-11-08 12:04:52.294540','ndimuthu7@gmail.com','2025-11-15 12:04:52.293231',7,'CONTRIBUTOR','ACCEPTED','047f9d48-e11b-4989-b575-78ec5c6dd556',5),(22,'2025-11-08 14:59:37.096502','ndimuthu7@gmail.com','2025-11-15 14:59:37.091225',4,'CONTRIBUTOR','ACCEPTED','7345fa71-b205-437a-a058-7d6da8cb137a',2);
/*!40000 ALTER TABLE `project_invitations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_user_roles`
--

DROP TABLE IF EXISTS `project_user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_user_roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role` enum('ADMIN','CONTRIBUTOR','MANAGER','VIEWER') NOT NULL,
  `project_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkj9cw2v63nttqmm7lqverjqrd` (`project_id`,`user_id`),
  KEY `FK12u4mwmc46n6so8179ldbegmk` (`user_id`),
  CONSTRAINT `FK12u4mwmc46n6so8179ldbegmk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK3698lh5lnjf1wrn8qpm6ccnpm` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_user_roles`
--

LOCK TABLES `project_user_roles` WRITE;
/*!40000 ALTER TABLE `project_user_roles` DISABLE KEYS */;
INSERT INTO `project_user_roles` VALUES (1,'MANAGER',2,7),(24,'MANAGER',3,7),(27,'MANAGER',3,8),(29,'MANAGER',5,7),(33,'CONTRIBUTOR',5,8),(34,'CONTRIBUTOR',2,8);
/*!40000 ALTER TABLE `project_user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projects`
--

DROP TABLE IF EXISTS `projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `projects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `artifact_count` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `icon_url` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `client_email` varchar(150) DEFAULT NULL,
  `client_phone` varchar(150) DEFAULT NULL,
  `project_name` varchar(150) DEFAULT NULL,
  `client_name` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5brqsoho9qc97d54l39n7osde` (`project_name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projects`
--

LOCK TABLES `projects` WRITE;
/*!40000 ALTER TABLE `projects` DISABLE KEYS */;
INSERT INTO `projects` VALUES (2,0,'2025-11-07 13:14:41.899722','this is demo description','www.ruhuan.com',10000,'2025-11-07 13:14:41.899756','kalana@gmail.com','0763257232','Rextro 2025','Dr.Kalana'),(3,0,'2025-11-07 13:29:55.311640','this is demo ecom platform','www.icon.com',14000,'2025-11-07 16:55:47.928521','james@gmail.com','0785462452','Ecommerce','James Joe'),(5,0,'2025-11-07 17:32:43.404436','this is demo lms project','www.icon.com',25000,'2025-11-07 17:32:43.404444','cham@gmail.com','07643253432','University LMS','Dr.Chaminda');
/*!40000 ALTER TABLE `projects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cognito_sub` varchar(50) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(150) NOT NULL,
  `full_name` varchar(150) DEFAULT NULL,
  `global_role` varchar(50) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKicq7oo5rrd402yxcuuvwmaag` (`cognito_sub`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'194ae50c-2051-7045-a21b-4a98a924251d','2025-10-23 19:58:54.571184','kvcniluminda@gmail.com','KVC Niluminda','APP_USER','2025-10-23 19:58:54.571196'),(2,'492a957c-a001-70a6-dfc4-98054157cef1','2025-10-23 20:12:25.315217','ad@psms.com','Company Owner','APP_ADMIN','2025-10-23 20:12:25.315258'),(4,'c03ca93c-7081-70e1-640d-150e0d0bfea7','2025-11-03 19:36:02.378306','admin@psms.com','Super Admin','APP_ADMIN','2025-11-03 19:36:02.378318'),(5,'b02cf94c-c0c1-7066-dbc8-c138d2d77b5c','2025-11-07 17:35:06.612713','niroshanb777@gmail.com','Buddhika Niroshan','APP_USER','2025-11-07 17:35:06.612721'),(6,'50ec897c-80a1-701d-f5c4-58d393b4a96d','2025-11-08 05:18:09.571991','jayathubandaraos@gmail.com','Jayathu Bandara','APP_USER','2025-11-08 05:18:09.572017'),(7,'e01c495c-d0e1-7089-bb69-84abf38f7143','2025-11-08 05:20:51.134231','niroshanb14@gmail.com','Dimuthu Niroshan','APP_USER','2025-11-08 05:20:51.134241'),(8,'c0cc59cc-e0f1-7026-8e60-6e19e432a9b4','2025-11-08 07:24:51.997322','ndimuthu7@gmail.com','Niroshan Kumarasinghe','APP_USER','2025-11-08 07:24:51.997377'),(9,'30acb9dc-9011-700a-bb1e-b69f57edb002','2025-11-08 15:32:16.881934','buddhikan410@gmail.com','Buddhika Niroshan','APP_USER','2025-11-08 15:32:16.881970');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-08 22:03:59
