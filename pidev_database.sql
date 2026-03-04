-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3307
-- Generation Time: Mar 04, 2026 at 12:09 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pidev`
--
CREATE DATABASE IF NOT EXISTS `pidev` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `pidev`;

-- --------------------------------------------------------

--
-- Table structure for table `admin_rewards`
--

CREATE TABLE `admin_rewards` (
  `id` int(11) NOT NULL,
  `admin_id` int(11) NOT NULL,
  `total_stars` int(11) NOT NULL DEFAULT 0,
  `total_points` int(11) NOT NULL DEFAULT 0,
  `streak_days` int(11) NOT NULL DEFAULT 0,
  `last_completion_date` date DEFAULT NULL,
  `task_finisher_badge` tinyint(1) NOT NULL DEFAULT 0,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `admin_tasks`
--

CREATE TABLE `admin_tasks` (
  `id` int(11) NOT NULL,
  `title` varchar(180) NOT NULL,
  `description` text DEFAULT NULL,
  `status` enum('TODO','DOING','DONE') NOT NULL DEFAULT 'TODO',
  `priority` enum('LOW','MEDIUM','HIGH','URGENT') NOT NULL DEFAULT 'MEDIUM',
  `tags` varchar(180) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `created_by` int(11) NOT NULL,
  `assigned_to` int(11) NOT NULL,
  `stars_earned` int(11) NOT NULL DEFAULT 0,
  `completed_at` datetime DEFAULT NULL,
  `position_idx` int(11) NOT NULL DEFAULT 1,
  `auto_generated` tinyint(1) NOT NULL DEFAULT 0,
  `template_code` varchar(60) DEFAULT NULL,
  `external_ref` varchar(120) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `admin_task_history`
--

CREATE TABLE `admin_task_history` (
  `id` int(11) NOT NULL,
  `task_id` int(11) NOT NULL,
  `actor_admin_id` int(11) NOT NULL,
  `action` varchar(40) NOT NULL,
  `from_status` varchar(20) DEFAULT NULL,
  `to_status` varchar(20) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `stars_earned` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `alerte`
--

CREATE TABLE `alerte` (
  `idAlerte` int(11) NOT NULL,
  `idCategorie` int(11) NOT NULL,
  `message` varchar(512) NOT NULL,
  `seuil` double NOT NULL,
  `active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `categorie`
--

CREATE TABLE `categorie` (
  `idCategorie` int(11) NOT NULL,
  `nomCategorie` varchar(255) NOT NULL,
  `budgetPrevu` double NOT NULL,
  `seuilAlerte` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `chat_messages`
--

CREATE TABLE `chat_messages` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `message` text NOT NULL,
  `sender` enum('USER','BOT') NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `cheque`
--

CREATE TABLE `cheque` (
  `id_cheque` int(11) NOT NULL,
  `numero_cheque` varchar(20) NOT NULL,
  `montant` double NOT NULL,
  `date_emission` datetime NOT NULL,
  `date_presentation` datetime DEFAULT NULL,
  `statut` varchar(20) NOT NULL,
  `id_wallet` int(11) NOT NULL,
  `beneficiaire` varchar(100) DEFAULT NULL,
  `motif_rejet` varchar(255) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `clients`
--

CREATE TABLE `clients` (
  `id` int(11) NOT NULL,
  `nom` varchar(50) DEFAULT NULL,
  `prenom` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `solde` decimal(15,2) DEFAULT NULL,
  `nb_cheques_refuses` int(11) DEFAULT 0,
  `nb_jours_negatifs` int(11) DEFAULT 0,
  `retraits_eleves` int(11) DEFAULT 0,
  `date_inscription` date DEFAULT NULL,
  `dernier_score` int(11) DEFAULT NULL,
  `niveau_risque` enum('FAIBLE','MOYEN','ELEVE') DEFAULT 'MOYEN',
  `privilege` enum('STANDARD','PREMIUM','VIP') DEFAULT 'STANDARD'
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `feedback`
--

CREATE TABLE `feedback` (
  `id_feedback` int(11) NOT NULL,
  `id_publication` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `commentaire` text DEFAULT NULL,
  `type_reaction` varchar(20) DEFAULT NULL,
  `date_feedback` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `game_sessions`
--

CREATE TABLE `game_sessions` (
  `id` varchar(64) NOT NULL,
  `user_id` int(11) NOT NULL,
  `context` enum('PROFILE','KYC','CHATBOT') NOT NULL DEFAULT 'PROFILE',
  `game_type` varchar(20) NOT NULL,
  `started_at` datetime NOT NULL DEFAULT current_timestamp(),
  `ended_at` datetime DEFAULT NULL,
  `duration_ms` bigint(20) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  `moves` int(11) DEFAULT NULL,
  `is_valid` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `gamification_events`
--

CREATE TABLE `gamification_events` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `event_code` varchar(80) NOT NULL,
  `event_label` varchar(160) NOT NULL,
  `points` int(11) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `historique_scores`
--

CREATE TABLE `historique_scores` (
  `id` int(11) NOT NULL,
  `client_id` int(11) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  `date_calcul` date DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `item`
--

CREATE TABLE `item` (
  `idItem` int(11) NOT NULL,
  `libelle` varchar(255) NOT NULL,
  `montant` double NOT NULL,
  `categorie` varchar(255) DEFAULT NULL,
  `idCategorie` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `kyc`
--

CREATE TABLE `kyc` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `cin` varchar(20) NOT NULL,
  `adresse` varchar(255) NOT NULL,
  `date_naissance` date NOT NULL,
  `signature_path` varchar(255) DEFAULT NULL,
  `signature_uploaded_at` datetime DEFAULT NULL,
  `statut` enum('EN_ATTENTE','APPROUVE','REFUSE') NOT NULL DEFAULT 'EN_ATTENTE',
  `commentaire_admin` text DEFAULT NULL,
  `date_submission` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `kyc_files`
--

CREATE TABLE `kyc_files` (
  `id` int(11) NOT NULL,
  `kyc_id` int(11) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  `file_type` varchar(20) NOT NULL,
  `file_size` bigint(20) NOT NULL,
  `file_data` longblob NOT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `loan`
--

CREATE TABLE `loan` (
  `loanId` int(11) NOT NULL,
  `loanType` varchar(50) NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `duration` int(11) NOT NULL,
  `interest_rate` decimal(5,2) NOT NULL,
  `remaining_principal` decimal(12,2) NOT NULL,
  `status` varchar(20) NOT NULL,
  `createdAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `id_user` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `messenger_messages`
--

CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL,
  `body` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `headers` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `queue_name` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `available_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `delivered_at` datetime DEFAULT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `type` varchar(30) NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(4) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `otp_audit`
--

CREATE TABLE `otp_audit` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `email` varchar(190) DEFAULT NULL,
  `channel` varchar(20) NOT NULL,
  `event_type` varchar(20) NOT NULL,
  `request_id` varchar(64) DEFAULT NULL,
  `success` tinyint(1) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `validation_seconds` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `password_reset`
--

CREATE TABLE `password_reset` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `code_hash` varchar(255) NOT NULL,
  `expires_at` datetime NOT NULL,
  `used_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `attempts` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `product`
--

CREATE TABLE `product` (
  `productId` int(11) NOT NULL,
  `category` enum('COMPTE_COURANT','COMPTE_EPARGNE','COMPTE_PREMIUM','COMPTE_JEUNE','COMPTE_ENTREPRISE','CARTE_DEBIT','CARTE_CREDIT','CARTE_PREMIUM','CARTE_VIRTUELLE','EPARGNE_CLASSIQUE','EPARGNE_LOGEMENT','DEPOT_A_TERME','PLACEMENT_INVESTISSEMENT','ASSURANCE_VIE','ASSURANCE_HABITATION','ASSURANCE_VOYAGE') NOT NULL DEFAULT 'COMPTE_COURANT',
  `price` double NOT NULL,
  `description` varchar(500) NOT NULL,
  `createdAt` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `productsubscription`
--

CREATE TABLE `productsubscription` (
  `subscriptionId` int(11) NOT NULL,
  `client` int(11) NOT NULL,
  `product` int(11) NOT NULL,
  `type` enum('MONTHLY','ANNUAL','TRANSACTION','ONE_TIME') NOT NULL,
  `subscriptionDate` date NOT NULL,
  `expirationDate` date NOT NULL,
  `status` enum('DRAFT','ACTIVE','SUSPENDED','CLOSED') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `publication`
--

CREATE TABLE `publication` (
  `id_publication` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `contenu` text NOT NULL,
  `categorie` varchar(100) DEFAULT NULL,
  `statut` varchar(50) DEFAULT NULL,
  `est_visible` tinyint(1) DEFAULT 1,
  `date_publication` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `repayment`
--

CREATE TABLE `repayment` (
  `repayId` int(11) NOT NULL,
  `loanId` int(11) NOT NULL,
  `month` int(11) NOT NULL,
  `startingBalance` decimal(10,2) NOT NULL,
  `monthlyPayment` decimal(10,2) NOT NULL,
  `capitalPart` decimal(10,2) NOT NULL,
  `interestPart` decimal(10,2) NOT NULL,
  `remainingBalance` decimal(10,2) NOT NULL,
  `status` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `security_events`
--

CREATE TABLE `security_events` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `ip` varchar(80) DEFAULT NULL,
  `type` varchar(40) NOT NULL,
  `metadata` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `transaction`
--

CREATE TABLE `transaction` (
  `id_transaction` int(11) NOT NULL,
  `montant` double NOT NULL,
  `type` varchar(20) NOT NULL,
  `description` text DEFAULT NULL,
  `date_transaction` datetime NOT NULL,
  `id_wallet` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `currentKycId` int(11) DEFAULT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `numTel` varchar(20) DEFAULT NULL,
  `role` varchar(10) NOT NULL,
  `password` varchar(255) NOT NULL,
  `kycStatus` varchar(20) DEFAULT NULL,
  `createdAt` datetime NOT NULL,
  `status` enum('EN_ATTENTE','ACCEPTE','REFUSE') NOT NULL DEFAULT 'EN_ATTENTE',
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user_badges`
--

CREATE TABLE `user_badges` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `badge_code` varchar(80) NOT NULL,
  `badge_label` varchar(160) NOT NULL,
  `awarded_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user_gamification`
--

CREATE TABLE `user_gamification` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `points_total` int(11) NOT NULL DEFAULT 0,
  `level` varchar(20) NOT NULL DEFAULT 'STARTER',
  `badges` varchar(255) DEFAULT NULL,
  `last_daily_game_at` date DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user_login_audit`
--

CREATE TABLE `user_login_audit` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `email` varchar(190) DEFAULT NULL,
  `success` tinyint(1) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user_qr_tokens`
--

CREATE TABLE `user_qr_tokens` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `token` varchar(120) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `expires_at` datetime NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user_security_challenges`
--

CREATE TABLE `user_security_challenges` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `challenge_code` varchar(80) NOT NULL,
  `challenge_title` varchar(160) NOT NULL,
  `status` varchar(20) NOT NULL,
  `progress` int(11) NOT NULL DEFAULT 0,
  `target` int(11) NOT NULL DEFAULT 1,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wallet`
--

CREATE TABLE `wallet` (
  `id_wallet` int(11) NOT NULL,
  `id_user` int(11) DEFAULT NULL,
  `nom_proprietaire` varchar(100) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `code_acces` varchar(10) DEFAULT NULL,
  `est_actif` tinyint(1) DEFAULT 0,
  `solde` double NOT NULL,
  `plafond_decouvert` double DEFAULT 0,
  `devise` enum('TND','USD','EUR') NOT NULL,
  `statut` enum('DRAFT','ACTIVE','SUSPENDED','CLOSED') NOT NULL,
  `date_creation` datetime NOT NULL,
  `tentatives_echouees` int(11) DEFAULT 0,
  `date_derniere_tentative` datetime DEFAULT NULL,
  `est_bloque` tinyint(1) DEFAULT 0
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin_rewards`
--
ALTER TABLE `admin_rewards`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `admin_id` (`admin_id`);

--
-- Indexes for table `admin_tasks`
--
ALTER TABLE `admin_tasks`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_admin_task_external_ref` (`external_ref`),
  ADD KEY `idx_admin_task_status` (`status`),
  ADD KEY `idx_admin_task_assigned` (`assigned_to`),
  ADD KEY `fk_admin_task_creator` (`created_by`);

--
-- Indexes for table `admin_task_history`
--
ALTER TABLE `admin_task_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_admin_task_history_task` (`task_id`),
  ADD KEY `idx_admin_task_history_actor` (`actor_admin_id`);

--
-- Indexes for table `alerte`
--
ALTER TABLE `alerte`
  ADD PRIMARY KEY (`idAlerte`),
  ADD KEY `idCategorie` (`idCategorie`);

--
-- Indexes for table `categorie`
--
ALTER TABLE `categorie`
  ADD PRIMARY KEY (`idCategorie`);

--
-- Indexes for table `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_chat_user` (`user_id`);

--
-- Indexes for table `cheque`
--
ALTER TABLE `cheque`
  ADD PRIMARY KEY (`id_cheque`),
  ADD KEY `id_wallet` (`id_wallet`);

--
-- Indexes for table `clients`
--
ALTER TABLE `clients`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Indexes for table `feedback`
--
ALTER TABLE `feedback`
  ADD PRIMARY KEY (`id_feedback`),
  ADD UNIQUE KEY `unique_user_pub_type` (`id_user`,`id_publication`,`type_reaction`),
  ADD KEY `feedback_ibfk_1` (`id_publication`);

--
-- Indexes for table `game_sessions`
--
ALTER TABLE `game_sessions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_game_sessions_user` (`user_id`);

--
-- Indexes for table `gamification_events`
--
ALTER TABLE `gamification_events`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_gamification_event` (`user_id`,`event_code`),
  ADD KEY `idx_gamification_user` (`user_id`);

--
-- Indexes for table `historique_scores`
--
ALTER TABLE `historique_scores`
  ADD PRIMARY KEY (`id`),
  ADD KEY `client_id` (`client_id`);

--
-- Indexes for table `item`
--
ALTER TABLE `item`
  ADD PRIMARY KEY (`idItem`),
  ADD KEY `idCategorie` (`idCategorie`);

--
-- Indexes for table `kyc`
--
ALTER TABLE `kyc`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `cin` (`cin`),
  ADD UNIQUE KEY `ux_kyc_cin` (`cin`);

--
-- Indexes for table `kyc_files`
--
ALTER TABLE `kyc_files`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ux_kyc_file_name` (`kyc_id`,`file_name`);

--
-- Indexes for table `loan`
--
ALTER TABLE `loan`
  ADD PRIMARY KEY (`loanId`),
  ADD KEY `fk_user_loan` (`id_user`);

--
-- Indexes for table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_75EA56E0FB7336F0` (`queue_name`),
  ADD KEY `IDX_75EA56E0E3BD61CE` (`available_at`),
  ADD KEY `IDX_75EA56E016BA31DB` (`delivered_at`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `otp_audit`
--
ALTER TABLE `otp_audit`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_otp_audit_user` (`user_id`),
  ADD KEY `idx_otp_audit_email` (`email`),
  ADD KEY `idx_otp_audit_type` (`event_type`),
  ADD KEY `idx_otp_audit_created` (`created_at`);

--
-- Indexes for table `password_reset`
--
ALTER TABLE `password_reset`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_password_reset_user_created` (`user_id`,`created_at`),
  ADD KEY `idx_password_reset_expires` (`expires_at`);

--
-- Indexes for table `product`
--
ALTER TABLE `product`
  ADD PRIMARY KEY (`productId`);

--
-- Indexes for table `productsubscription`
--
ALTER TABLE `productsubscription`
  ADD PRIMARY KEY (`subscriptionId`),
  ADD KEY `fk_subscription_product` (`product`),
  ADD KEY `fk_sub_client` (`client`);

--
-- Indexes for table `publication`
--
ALTER TABLE `publication`
  ADD PRIMARY KEY (`id_publication`);

--
-- Indexes for table `repayment`
--
ALTER TABLE `repayment`
  ADD PRIMARY KEY (`repayId`),
  ADD KEY `fk_repayment_loan` (`loanId`);

--
-- Indexes for table `security_events`
--
ALTER TABLE `security_events`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `transaction`
--
ALTER TABLE `transaction`
  ADD PRIMARY KEY (`id_transaction`),
  ADD KEY `id_wallet` (`id_wallet`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `user_badges`
--
ALTER TABLE `user_badges`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_user_badge` (`user_id`,`badge_code`),
  ADD KEY `idx_user_badges_user` (`user_id`);

--
-- Indexes for table `user_gamification`
--
ALTER TABLE `user_gamification`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`);

--
-- Indexes for table `user_login_audit`
--
ALTER TABLE `user_login_audit`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_login_audit_user` (`user_id`),
  ADD KEY `idx_login_audit_email` (`email`),
  ADD KEY `idx_login_audit_created` (`created_at`);

--
-- Indexes for table `user_qr_tokens`
--
ALTER TABLE `user_qr_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `token` (`token`),
  ADD KEY `idx_qr_token_user` (`user_id`),
  ADD KEY `idx_qr_token_active_exp` (`active`,`expires_at`);

--
-- Indexes for table `user_security_challenges`
--
ALTER TABLE `user_security_challenges`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_user_challenge` (`user_id`,`challenge_code`),
  ADD KEY `idx_user_challenges_user` (`user_id`);

--
-- Indexes for table `wallet`
--
ALTER TABLE `wallet`
  ADD PRIMARY KEY (`id_wallet`),
  ADD KEY `id_user` (`id_user`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin_rewards`
--
ALTER TABLE `admin_rewards`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `admin_tasks`
--
ALTER TABLE `admin_tasks`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `admin_task_history`
--
ALTER TABLE `admin_task_history`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `alerte`
--
ALTER TABLE `alerte`
  MODIFY `idAlerte` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `categorie`
--
ALTER TABLE `categorie`
  MODIFY `idCategorie` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `chat_messages`
--
ALTER TABLE `chat_messages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `cheque`
--
ALTER TABLE `cheque`
  MODIFY `id_cheque` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `clients`
--
ALTER TABLE `clients`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `feedback`
--
ALTER TABLE `feedback`
  MODIFY `id_feedback` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `gamification_events`
--
ALTER TABLE `gamification_events`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `historique_scores`
--
ALTER TABLE `historique_scores`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `item`
--
ALTER TABLE `item`
  MODIFY `idItem` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `kyc`
--
ALTER TABLE `kyc`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `kyc_files`
--
ALTER TABLE `kyc_files`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `loan`
--
ALTER TABLE `loan`
  MODIFY `loanId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `otp_audit`
--
ALTER TABLE `otp_audit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `password_reset`
--
ALTER TABLE `password_reset`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `product`
--
ALTER TABLE `product`
  MODIFY `productId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `productsubscription`
--
ALTER TABLE `productsubscription`
  MODIFY `subscriptionId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `publication`
--
ALTER TABLE `publication`
  MODIFY `id_publication` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `repayment`
--
ALTER TABLE `repayment`
  MODIFY `repayId` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `security_events`
--
ALTER TABLE `security_events`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `transaction`
--
ALTER TABLE `transaction`
  MODIFY `id_transaction` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user_badges`
--
ALTER TABLE `user_badges`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user_gamification`
--
ALTER TABLE `user_gamification`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user_login_audit`
--
ALTER TABLE `user_login_audit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user_qr_tokens`
--
ALTER TABLE `user_qr_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user_security_challenges`
--
ALTER TABLE `user_security_challenges`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wallet`
--
ALTER TABLE `wallet`
  MODIFY `id_wallet` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `admin_rewards`
--
ALTER TABLE `admin_rewards`
  ADD CONSTRAINT `fk_admin_rewards_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `admin_tasks`
--
ALTER TABLE `admin_tasks`
  ADD CONSTRAINT `fk_admin_task_assigned` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_admin_task_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `admin_task_history`
--
ALTER TABLE `admin_task_history`
  ADD CONSTRAINT `fk_admin_task_history_actor` FOREIGN KEY (`actor_admin_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_admin_task_history_task` FOREIGN KEY (`task_id`) REFERENCES `admin_tasks` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `alerte`
--
ALTER TABLE `alerte`
  ADD CONSTRAINT `fk_alerte_categorie` FOREIGN KEY (`idCategorie`) REFERENCES `categorie` (`idCategorie`) ON DELETE CASCADE;

--
-- Constraints for table `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD CONSTRAINT `fk_chat_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `feedback`
--
ALTER TABLE `feedback`
  ADD CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`id_publication`) REFERENCES `publication` (`id_publication`) ON DELETE CASCADE,
  ADD CONSTRAINT `feedback_ibfk_2` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `game_sessions`
--
ALTER TABLE `game_sessions`
  ADD CONSTRAINT `fk_game_sessions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `gamification_events`
--
ALTER TABLE `gamification_events`
  ADD CONSTRAINT `fk_gamification_event_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `item`
--
ALTER TABLE `item`
  ADD CONSTRAINT `fk_item_categorie` FOREIGN KEY (`idCategorie`) REFERENCES `categorie` (`idCategorie`) ON UPDATE CASCADE;

--
-- Constraints for table `kyc`
--
ALTER TABLE `kyc`
  ADD CONSTRAINT `fk_kyc_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `kyc_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `kyc_files`
--
ALTER TABLE `kyc_files`
  ADD CONSTRAINT `kyc_files_ibfk_1` FOREIGN KEY (`kyc_id`) REFERENCES `kyc` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `loan`
--
ALTER TABLE `loan`
  ADD CONSTRAINT `fk_user_loan` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `otp_audit`
--
ALTER TABLE `otp_audit`
  ADD CONSTRAINT `fk_otp_audit_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `productsubscription`
--
ALTER TABLE `productsubscription`
  ADD CONSTRAINT `fk_sub_client` FOREIGN KEY (`client`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `fk_sub_produit` FOREIGN KEY (`product`) REFERENCES `product` (`productId`);

--
-- Constraints for table `repayment`
--
ALTER TABLE `repayment`
  ADD CONSTRAINT `fk_repayment_loan` FOREIGN KEY (`loanId`) REFERENCES `loan` (`loanId`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
