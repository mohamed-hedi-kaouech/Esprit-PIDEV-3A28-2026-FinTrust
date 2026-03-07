-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3307
-- Generation Time: Mar 07, 2026 at 01:43 PM
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

--
-- Dumping data for table `admin_tasks`
--

INSERT INTO `admin_tasks` (`id`, `title`, `description`, `status`, `priority`, `tags`, `due_date`, `created_by`, `assigned_to`, `stars_earned`, `completed_at`, `position_idx`, `auto_generated`, `template_code`, `external_ref`, `created_at`, `updated_at`) VALUES
(1, 'Verifier KYC en attente (1)', 'Des dossiers KYC sont en attente. Ouvrir Validation KYC pour traitement.', 'DONE', 'HIGH', 'KYC,COMPLIANCE', '2026-02-24', 27, 27, 0, NULL, 2, 1, 'TPL_KYC_REVIEW', 'KYC_PENDING_REVIEW', '2026-02-23 22:16:52', '2026-02-23 22:48:57'),
(3, 'Review KYC', 'pour 3 users', 'DONE', 'HIGH', '', NULL, 27, 27, 0, NULL, 1, 0, NULL, NULL, '2026-02-23 22:17:30', '2026-02-23 22:38:41');

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

--
-- Dumping data for table `admin_task_history`
--

INSERT INTO `admin_task_history` (`id`, `task_id`, `actor_admin_id`, `action`, `from_status`, `to_status`, `note`, `stars_earned`, `created_at`) VALUES
(1, 3, 27, 'CREATE', NULL, 'TODO', 'Creation manuelle', 0, '2026-02-23 22:17:30'),
(2, 3, 27, 'MOVE', 'TODO', 'DOING', 'Changement colonne kanban', 0, '2026-02-23 22:17:37'),
(3, 3, 27, 'MOVE', 'DOING', 'DONE', 'Changement colonne kanban', 0, '2026-02-23 22:38:41'),
(4, 1, 27, 'MOVE', 'TODO', 'DOING', 'Changement colonne kanban', 0, '2026-02-23 22:38:44'),
(5, 1, 27, 'MOVE', 'TODO', 'DOING', 'Changement colonne kanban', 0, '2026-02-23 22:38:47'),
(6, 1, 27, 'MOVE', 'TODO', 'DONE', 'Changement colonne kanban', 0, '2026-02-23 22:48:47');

-- --------------------------------------------------------

--
-- Table structure for table `ah`
--

CREATE TABLE `ah` (
  `id` int(11) NOT NULL
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

--
-- Dumping data for table `alerte`
--

INSERT INTO `alerte` (`idAlerte`, `idCategorie`, `message`, `seuil`, `active`, `created_at`) VALUES
(25, 3, 'Seuil d\'alerte atteint par ajout d\'item', 2900, 1, '2026-03-03 08:19:38'),
(26, 3, 'La catégorie \'TEST\' a atteint le seuil (2900.00 DT). Montant actuel: 3000.00 DT.', 2900, 1, '2026-03-03 08:19:47');

-- --------------------------------------------------------

--
-- Table structure for table `author`
--

CREATE TABLE `author` (
  `id` int(11) NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `nbr_books` int(11) NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `author`
--

INSERT INTO `author` (`id`, `username`, `nbr_books`, `email`) VALUES
(3, 'farah', 403, 'Abid.Farah@esprit.tn'),
(1235, 'feryel', 20, 'Hajji.feryel@esprit.tn'),
(1236, 'maryem', 3, 'said.maryem@esprit.tn');

-- --------------------------------------------------------

--
-- Table structure for table `book`
--

CREATE TABLE `book` (
  `id` int(11) NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `published` tinyint(1) NOT NULL,
  `publication_date` date NOT NULL,
  `author_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `book`
--

INSERT INTO `book` (`id`, `title`, `category`, `published`, `publication_date`, `author_id`) VALUES
(4, 'Book2', 'histoire', 0, '2024-01-01', 3),
(5, 'Book2', 'science', 1, '2023-01-01', 3),
(6, 'Book3', 'science', 1, '2024-01-01', 1235),
(7, 'Book4', 'fiction', 1, '2023-01-01', 1236),
(8, 'Book7', 'science', 1, '2025-01-01', 3);

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

--
-- Dumping data for table `categorie`
--

INSERT INTO `categorie` (`idCategorie`, `nomCategorie`, `budgetPrevu`, `seuilAlerte`) VALUES
(3, 'TEST', 3000, 2900);

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

--
-- Dumping data for table `chat_messages`
--

INSERT INTO `chat_messages` (`id`, `user_id`, `message`, `sender`, `created_at`) VALUES
(1, 30, 'bonsoir comment vas tu', 'USER', '2026-02-23 14:12:42'),
(2, 30, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-02-23 14:12:42'),
(3, 30, 'mot de passe', 'USER', '2026-02-23 14:12:53'),
(4, 30, 'Pour reinitialiser le mot de passe: page Connexion > \'Mot de passe oublie ?\' puis suivez le code recu par email.', 'BOT', '2026-02-23 14:12:53'),
(5, 30, 'parlez moi de fintrust', 'USER', '2026-02-23 14:13:05'),
(6, 30, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-02-23 14:13:05'),
(7, 30, 'Comment reinitialiser mon mot de passe ?', 'USER', '2026-02-23 14:18:54'),
(8, 30, 'Pour reinitialiser le mot de passe: page Connexion > \'Mot de passe oublie ?\' puis suivez le code recu par email.', 'BOT', '2026-02-23 14:18:54'),
(9, 30, 'Je veux changer mon email', 'USER', '2026-02-23 14:18:56'),
(10, 30, 'Pour changer votre email: Dashboard Client > Profil > modifier email > Enregistrer.', 'BOT', '2026-02-23 14:18:56'),
(11, 30, 'Mon compte est bloque', 'USER', '2026-02-23 14:18:57'),
(12, 30, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-02-23 14:18:57'),
(13, 30, 'coucou', 'USER', '2026-02-23 14:19:03'),
(14, 30, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-02-23 14:19:03'),
(15, 54, 'pizza', 'USER', '2026-02-24 09:57:02'),
(16, 54, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-02-24 09:57:02'),
(17, 30, 'Je veux changer mon email', 'USER', '2026-02-27 22:09:19'),
(18, 30, 'Pour changer votre email: Dashboard Client > Profil > modifier email > Enregistrer.', 'BOT', '2026-02-27 22:09:19'),
(19, 30, 'Je veux changer mon email', 'USER', '2026-02-27 22:09:25'),
(20, 30, 'Pour changer votre email: Dashboard Client > Profil > modifier email > Enregistrer.', 'BOT', '2026-02-27 22:09:25'),
(25, 62, 'Mon compte est bloque', 'USER', '2026-02-28 15:37:25'),
(26, 62, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-02-28 15:37:25'),
(27, 65, 'test', 'USER', '2026-03-02 11:01:32'),
(28, 65, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-03-02 11:01:32'),
(29, 65, 'how to make pizza', 'USER', '2026-03-02 22:47:43'),
(30, 65, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-03-02 22:47:43'),
(31, 65, 'Mon compte est bloque', 'USER', '2026-03-02 22:47:49'),
(32, 65, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-03-02 22:47:49'),
(33, 65, 'Mon compte est bloque', 'USER', '2026-03-02 22:47:53'),
(34, 65, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-03-02 22:47:53'),
(35, 65, 'Je veux changer mon email', 'USER', '2026-03-02 22:47:55'),
(36, 65, 'Pour changer votre email: Dashboard Client > Profil > modifier email > Enregistrer.', 'BOT', '2026-03-02 22:47:55'),
(37, 65, 'Mon compte est bloque', 'USER', '2026-03-02 22:48:02'),
(38, 65, 'Je n\'ai pas compris. Essayez: \'mot de passe\', \'compte bloque\', \'changer mon email\', \'notifications\'.', 'BOT', '2026-03-02 22:48:02'),
(39, 65, 'Comment reinitialiser mon mot de passe ?', 'USER', '2026-03-02 22:48:10'),
(40, 65, 'Pour reinitialiser le mot de passe: page Connexion > \'Mot de passe oublie ?\' puis suivez le code recu par email.', 'BOT', '2026-03-02 22:48:10');

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

--
-- Dumping data for table `cheque`
--

INSERT INTO `cheque` (`id_cheque`, `numero_cheque`, `montant`, `date_emission`, `date_presentation`, `statut`, `id_wallet`, `beneficiaire`, `motif_rejet`) VALUES
(1, '1', 30000, '2026-02-21 04:18:40', NULL, 'RESERVE', 71, 'mourad hajji', NULL),
(2, '\"', 4000, '2026-02-21 04:23:01', NULL, 'REJETE', 71, 'khaled gedria', 'Montant trop elevé'),
(3, '123', 300, '2026-02-21 04:57:43', '2026-02-21 05:03:52', 'PAYE', 71, 'hajji mourad', NULL),
(4, '145', 230, '2026-02-21 05:51:14', NULL, 'REJETE', 71, 'hajji mourad', 'vous etes deja en rouge'),
(5, '1556', 234, '2026-02-21 05:59:39', NULL, 'REJETE', 71, 'hmani ines', 'solde ins'),
(6, '1487', 200, '2026-02-22 15:32:23', NULL, 'REJETE', 71, 'hajji mourad', 'solde ins'),
(7, '1278', 2300, '2026-02-27 22:21:19', NULL, 'EMIS', 71, 'hmani ines', NULL),
(8, '123', 200, '2026-03-03 08:44:59', NULL, 'EMIS', 127, 'hmani.ines@esprit.tn', NULL),
(9, '1234', 230, '2026-03-03 09:11:15', '2026-03-03 09:14:13', 'PAYE', 128, 'hmani.ines@esprit.tn', NULL);

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

--
-- Dumping data for table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\Version20251007134608', '2025-10-07 15:47:43', 86),
('DoctrineMigrations\\Version20251014135353', '2025-10-14 15:54:22', 50),
('DoctrineMigrations\\Version20251021130224', '2025-10-21 15:03:37', 65),
('DoctrineMigrations\\Version20251021131914', '2025-10-21 15:19:39', 94),
('DoctrineMigrations\\Version20251021144219', '2025-10-21 16:42:23', 90);

-- --------------------------------------------------------

--
-- Table structure for table `faq`
--

CREATE TABLE `faq` (
  `id` int(11) NOT NULL,
  `question` varchar(255) NOT NULL,
  `answer` text NOT NULL,
  `keywords` varchar(500) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `faq`
--

INSERT INTO `faq` (`id`, `question`, `answer`, `keywords`) VALUES
(1, 'Comment reinitialiser mon mot de passe ?', 'Allez sur la page Connexion puis cliquez sur \'Mot de passe oublie ?\'.', 'reset,password,mot de passe,oublie,reinitialiser'),
(2, 'Comment voir mes notifications ?', 'Dans Dashboard Client, cliquez sur \'Voir historique\' dans la zone Notifications.', 'notification,historique,message,alerte'),
(3, 'Comment modifier mon profil ?', 'Ouvrez Dashboard Client > Profil, modifiez les champs puis cliquez sur Modifier.', 'profil,modifier,email,telephone,nom'),
(4, 'Pourquoi mon acces est limite ?', 'Votre KYC doit etre approuve pour debloquer toutes les sections (Wallet, Loan, Budget...).', 'kyc,acces limite,refuse,en attente,approuve');

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

--
-- Dumping data for table `feedback`
--

INSERT INTO `feedback` (`id_feedback`, `id_publication`, `id_user`, `commentaire`, `type_reaction`, `date_feedback`) VALUES
(54, 1, 1, '3', 'RATE', '2026-02-24 00:32:52'),
(55, 1, 1, 'magnifque!!!!', 'COMMENT', '2026-02-24 00:33:00'),
(57, 4, 1, '', 'DISLIKE', '2026-02-24 00:33:03'),
(58, 4, 1, '4', 'RATE', '2026-02-24 00:33:06'),
(59, 4, 1, 'tres bien!!', 'COMMENT', '2026-02-24 00:33:15'),
(60, 5, 1, '4', 'RATE', '2026-02-24 01:48:02'),
(62, 5, 1, '', 'DISLIKE', '2026-02-24 01:48:07'),
(68, 6, 1, '2', 'RATE', '2026-02-24 02:13:23'),
(70, 16, 1, '2', 'RATE', '2026-02-24 03:41:26'),
(71, 12, 1, '5', 'RATE', '2026-02-24 03:41:29'),
(81, 16, 1, 'magnifique', 'COMMENT', '2026-02-24 03:50:15'),
(83, 8, 1, 'magnifique', 'COMMENT', '2026-02-24 03:53:45'),
(86, 14, 1, 'j\'ai pas aimé!', 'COMMENT', '2026-02-24 03:59:34'),
(90, 37, 1, '2', 'RATE', '2026-02-24 09:38:15'),
(91, 38, 1, '4', 'RATE', '2026-02-24 09:38:17'),
(96, 38, 1, 'Je vais m’en prendre à toi si tu ne supprimes pas ça.', 'COMMENT', '2026-02-27 22:20:27'),
(98, 12, 1, 'C\'est génial!!', 'COMMENT', '2026-02-27 22:38:31'),
(103, 9, 1, '2', 'RATE', '2026-02-27 22:44:49'),
(104, 8, 1, '4', 'RATE', '2026-02-27 22:44:51'),
(105, 14, 1, '1', 'RATE', '2026-02-27 22:44:53'),
(109, 8, 1, '', 'DISLIKE', '2026-02-27 22:45:00'),
(112, 1, 1, '', 'LIKE', '2026-02-27 23:14:36'),
(113, 14, 1, 'REPLY_TO:86|Merci', 'ADMIN_REPLY', '2026-02-27 23:18:53'),
(114, 14, 1, '', 'LIKE', '2026-02-27 23:19:27'),
(116, 37, 1, 'Tres bien!!', 'COMMENT', '2026-02-27 23:28:23'),
(118, 37, 1, '', 'LIKE', '2026-02-27 23:28:30'),
(119, 37, 1, 'REPLY_TO:116|Merci', 'ADMIN_REPLY', '2026-02-27 23:29:30'),
(123, 12, 64, '2', 'RATE', '2026-03-01 17:36:09'),
(125, 5, 64, 'tres bien!!!', 'COMMENT', '2026-03-01 17:36:26'),
(126, 38, 64, '', 'LIKE', '2026-03-01 17:36:48'),
(127, 12, 64, '', 'LIKE', '2026-03-01 17:36:53'),
(128, 38, 64, '2', 'RATE', '2026-03-01 17:36:59'),
(129, 8, 64, '3', 'RATE', '2026-03-01 17:37:02'),
(130, 1, 64, '5', 'RATE', '2026-03-01 17:37:04'),
(132, 16, 64, '3', 'RATE', '2026-03-01 17:37:13'),
(134, 9, 64, '', 'LIKE', '2026-03-01 17:37:17'),
(136, 16, 15, 'REPLY_TO:81|merci', 'ADMIN_REPLY', '2026-03-01 17:47:33'),
(140, 40, 64, '', 'LIKE', '2026-03-01 17:51:59'),
(142, 40, 64, '3', 'RATE', '2026-03-01 18:18:31'),
(143, 40, 64, 'Parfait!!', 'COMMENT', '2026-03-01 19:22:39'),
(144, 36, 64, '', 'LIKE', '2026-03-01 19:23:25'),
(146, 14, 64, '', 'LIKE', '2026-03-01 19:23:33'),
(147, 6, 64, '', 'DISLIKE', '2026-03-01 19:23:43'),
(148, 16, 64, '', 'DISLIKE', '2026-03-01 19:23:47'),
(149, 1, 67, '2', 'RATE', '2026-03-02 00:50:22'),
(150, 40, 67, '', 'LIKE', '2026-03-02 00:50:30'),
(151, 12, 67, '', 'LIKE', '2026-03-02 00:50:35'),
(152, 4, 67, '', 'DISLIKE', '2026-03-02 00:50:40'),
(153, 5, 67, '', 'DISLIKE', '2026-03-02 00:50:43'),
(154, 38, 67, '', 'LIKE', '2026-03-02 00:50:47'),
(155, 8, 67, '1', 'RATE', '2026-03-02 00:50:54'),
(156, 37, 67, '2', 'RATE', '2026-03-02 00:50:58'),
(157, 16, 67, '2', 'RATE', '2026-03-02 00:51:01'),
(158, 9, 67, '1', 'RATE', '2026-03-02 00:51:04'),
(159, 14, 67, '3', 'RATE', '2026-03-02 00:51:06'),
(167, 12, 15, '', 'DISLIKE', '2026-03-02 14:39:28'),
(170, 1, 15, 'REPLY_TO:55|bien', 'ADMIN_REPLY', '2026-03-02 14:40:15'),
(171, 1, 15, '', 'LIKE', '2026-03-02 14:40:32'),
(174, 4, 15, '3', 'RATE', '2026-03-02 14:43:59'),
(175, 5, 15, '', 'LIKE', '2026-03-02 15:00:17'),
(176, 5, 15, '4', 'RATE', '2026-03-02 15:00:20'),
(177, 37, 15, '', 'LIKE', '2026-03-02 15:00:26'),
(178, 37, 15, '2', 'RATE', '2026-03-02 15:00:29'),
(179, 40, 15, '', 'DISLIKE', '2026-03-02 15:07:06'),
(180, 2, 15, '', 'LIKE', '2026-03-02 22:51:48'),
(181, 41, 15, '', 'LIKE', '2026-03-02 22:51:50'),
(182, 40, 15, 'I WANT TO K*LL YOU', 'COMMENT', '2026-03-03 09:25:47'),
(183, 40, 15, '3', 'RATE', '2026-03-03 09:30:22'),
(187, 38, 15, '5', 'RATE', '2026-03-03 09:30:54'),
(188, 1, 15, '5', 'RATE', '2026-03-03 09:31:02'),
(189, 12, 15, '5', 'RATE', '2026-03-03 09:31:15');

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

--
-- Dumping data for table `game_sessions`
--

INSERT INTO `game_sessions` (`id`, `user_id`, `context`, `game_type`, `started_at`, `ended_at`, `duration_ms`, `score`, `moves`, `is_valid`) VALUES
('738f9b60-414d-471e-bfdf-3ced69829262', 30, 'PROFILE', 'MEMORY', '2026-02-23 22:48:01', '2026-02-23 22:48:14', 12796, 50, 6, 1);

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

--
-- Dumping data for table `gamification_events`
--

INSERT INTO `gamification_events` (`id`, `user_id`, `event_code`, `event_label`, `points`, `created_at`) VALUES
(4, 30, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-02-23 21:33:53'),
(5, 30, 'FIRST_SECURE_LOGIN', 'Premiere connexion securisee', 15, '2026-02-23 21:33:53'),
(6, 30, 'ACTIVE_PARTICIPATION', 'Participation active', 50, '2026-02-23 21:33:53'),
(7, 26, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-02-23 21:33:53'),
(8, 15, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-02-23 21:33:53'),
(10, 23, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-02-23 21:33:53'),
(151, 46, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-02-23 23:05:05'),
(285, 30, 'GAME_SESSION_dcf780c3_5b8e_413f_b2da_01dc5e81e112', 'Memory Game Session', 7, '2026-02-23 23:52:31'),
(352, 30, 'GAME_SESSION_8dd97c41_34c6_4b91_b32e_e58e48c517ce', 'Memory Game Session', 7, '2026-02-24 00:35:33'),
(842, 54, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-02-24 09:24:54'),
(843, 54, 'MFA_ENABLED', 'Activation MFA', 20, '2026-02-24 09:24:54'),
(855, 54, 'FIRST_SECURE_LOGIN', 'Premiere connexion securisee', 15, '2026-02-24 09:36:45'),
(889, 55, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-02-24 09:54:23'),
(890, 55, 'MFA_ENABLED', 'Activation MFA', 20, '2026-02-24 09:54:23'),
(1791, 62, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-02-28 17:51:48'),
(1792, 62, 'FIRST_SECURE_LOGIN', 'Premiere connexion securisee', 15, '2026-02-28 17:51:48'),
(1871, 63, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-02-28 18:11:00'),
(1872, 63, 'FIRST_SECURE_LOGIN', 'Premiere connexion securisee', 15, '2026-02-28 18:11:00'),
(2287, 65, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-03-01 15:50:26'),
(2288, 65, 'FIRST_SECURE_LOGIN', 'Premiere connexion securisee', 15, '2026-03-01 15:50:26'),
(2559, 66, 'PROFILE_COMPLETED', 'Profil complete a 100%', 10, '2026-03-01 16:31:58'),
(2560, 66, 'FIRST_SECURE_LOGIN', 'Premiere connexion securisee', 15, '2026-03-01 16:31:58'),
(2867, 65, 'ACTIVE_PARTICIPATION', 'Participation active', 50, '2026-03-02 10:33:22'),
(3126, 65, 'MFA_ENABLED', 'Activation MFA', 20, '2026-03-02 11:09:36'),
(5042, 63, 'ACTIVE_PARTICIPATION', 'Participation active', 50, '2026-03-03 09:06:35');

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

--
-- Dumping data for table `item`
--

INSERT INTO `item` (`idItem`, `libelle`, `montant`, `categorie`, `idCategorie`) VALUES
(26, 'ITEM', 1200, NULL, 3),
(27, 'item', 1200, NULL, 3);

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

--
-- Dumping data for table `kyc`
--

INSERT INTO `kyc` (`id`, `user_id`, `cin`, `adresse`, `date_naissance`, `signature_path`, `signature_uploaded_at`, `statut`, `commentaire_admin`, `date_submission`) VALUES
(3, 65, '12345678', 'ras jebel', '2004-03-02', NULL, NULL, 'APPROUVE', NULL, '2026-03-02 22:45:55'),
(4, 66, '12345675', 'hedi	e', '2002-03-01', 'storage/kyc/signatures/signature_user_66_1772379175292.png', '2026-03-01 16:32:55', 'APPROUVE', NULL, '2026-03-01 16:33:24'),
(5, 63, 'TMP-KYC-63', '', '1970-01-01', NULL, NULL, 'APPROUVE', NULL, '2026-03-03 08:41:30');

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

--
-- Dumping data for table `kyc_files`
--

INSERT INTO `kyc_files` (`id`, `kyc_id`, `file_name`, `file_path`, `file_type`, `file_size`, `file_data`, `updated_at`) VALUES
(2, 3, 'configuration_RZ-4.PNG', NULL, 'image/png', 43283, 0x89504e470d0a1a0a0000000d49484452000002740000028408060000008efba7d6000000017352474200aece1ce90000000467414d410000b18f0bfc6105000000097048597300000ec300000ec301c76fa8640000a8a849444154785eedfd4b8eec3cd0a609feebf15c4aac24263569f4a0ab27856e3850e84921518344a3463148e4650bdfe2bea2f1262369bc48728f90229ec183735c146f468af6bac983fc8ffff49ffed3bffff11fff010000000077054107000000707310740000000037074107000000707310740000000037e78a82eef3ebf9efc7e3f3dfafe787990e000000008a2ce83e9efffef3cf3f055fcfcf7f1feae6c7e7577bcfe7632b6c998f7f9f92df146c2eedcbd5ebda73acecf37c3c753fbffe7d7ed4e947fb0d000000f00674844e04db26541e8d7029d383f0a9c5ce9cc7bf9f5f5fff7e7e74227031322775ed2ffb0548fd5fcf42c87e273e3a695c07000000e8d217748ec16bcfe6de45b20834ca1601a9a37f9ee21e11992a7af6f5f9ef53c4e1634bff54e95f2e3d892369afbff67cfefb95f397e2c9acdf9591d27514d3eebb4417533e57f667b87fc94e628f58b6e647442d000000dc8baea07b84489a29287c14ab7c1dbb822f3f09b49e588caf5a45b86d422d10c4a0169cf2ea76bbcf47fe741e4957edf4a2ceff3e2f7c96f21ab115fb565cabe889d91079d4f65b14730a2274000000b09b5ad06dd1a14a3c65442459a243c455195d0a6cf75a11b046f074055d1067c5bd9a8e40d4e548ff0a816afd4eefa8a0b3f21df81d20820e00000076d38bd0f93f4a68c4c5206ab71743808d5fb996d1b606041d000000fc5546bfa193cffa376cad909944cd46740458aaa3797dea68fe48c3bf52dd7e67b6f2caf56d82ce51d47ff895ab16a0ae7dfffcdc1f68000000c04dc882ce899b1c15cb82268892f479fa47038be8726ac12369cf0f27c44c21e3daf354af8545fc14af85cb74116629dae5c569bcee459dea6f6a83ef6bbc96d111c23acda3236a4160faeb12691341b653d0f9c8682a5bcaa8442d00000040838ed05d81dfb4a9f047279207000000f052ae26e8ee8e8ef2d51b3303000000bc05041d000000c0cd41d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d0010000bc96de06f4006f23093abdf16ec1818d837f3b729cd866a3f628b4fa448bbb71f7f65f83b81135cfcff7616c166e71c7f9fdfaf5f97df3f3887d53ffeab5741f8f626d966da3642f502953dae4afc57615f67c7ef4edebd6777f728fde785f70761b6f49d56e8e6fd5c13a0b2f4547e864c215136ce118ac9a5f7f16a9b7c97d8fe3e2acd8ef444e0e41d07d37cd3af64b78c5fa5cf2fdf373b4fe48ffce08ba706eb7b64f38e9289529e9c5f9e4ca7edab622fe529efa38497d36f8086b0ed6d7a4ac730216a0c21674ed832e69fe1bc5b33a9a2add230f47baae28266c3c7b35a5d51befea6f51fed82cb96f970071f9dc4392ca976f51cfe2819c1f0dd6ed9f23b5af40db691a2150fd97b25d1fd3bda9fe6df169cb99dae743fa1bcb97c8e1a73a6d63657ca6ed3f67bf3947c7cfd935d617faefea4d47a8f936ccd263f903fb2df5af98dfb258cb67fd1c55ed77ff3fe7906b4e8ecfe4f91c1136d4766395e6652cbf707e6f7ffe03d25773fe0ee6f7bbedf30ab67ed5f3ea55f3b34fda305ddb2daf87a98cd1fab1b0fe481f9e9feabe3d632fe54f4e17f2624ce655ba4f09baf2be6a5d2cd256049db3ab7974e546778e029ca11674f9613326ba4f97872c4e6899dcf5a41c7d03fb748b4d31c96581718ba2be272c0aae5c09834b3dee1effafbea783b4e7a9cf7695f2c5c9c4fc2b87f7cffad75b0434bd87d5d79fda671cde2ff96a81d59433b48f1386aa7fd6c2b412a11bb6ffacfd069c1a3fef3042fac30be5d08e50e642ba2f736cbf71ffa42d5b9a9425e9fa390a7675e3953ecb584ee6d21ece8ecfd2f339c0cf671139ca067abebdfbf94ff4e66fa297fe6efb9cc5b7eff0fa3c9f9f433a8249ca28e69c6364ff59842e3d97f2b9b6ff106badac486dcd6d7e93a01bf5df13eb2de612c02b588dd0a5f462a21b0f51f781f50e352e4605d5fd0b0fa68ddde6ccc282b4d2bfde22a0311f682b5f55fe52fd23fbb83a8a0882fb7fbd301d1674afb25f9793e3a7edebea95dfc5c8ff739b66e9727d62bf61ffacbeea3a3dd2475dfee6bc4e33b38ffbffb0fd923fb6ab643e5f128da07414f65f29dfb2e34ea4ce5119bdf477dbe72c5bbb0faccf4bf3738cac1d22427cb4cee57b3851a8bf302446f69f09ba6efb672cdc5baf158f4effcf093a3736c3392163f77d7306fe18c3dfd055ac3c70fd0776e2b0137b1ee282507ef75b8f3cbcaf10240b8ba069472b5f55fe52fd5dfbc842e2faa2225c4d798eab0bbac3e3a7edebeadd2fe8e6f61bf6cfeaab35e6394d5eb3bb76f4d2f7727a7c169fcf014341b75abe65c79d489da3327ae9efb6cf5956faf5b2f96910e652b083ffbfbc1e35e6dca89d6f13740b7d699e0569bf91678fa0abdb3cea7b8858577d0478253341a71fc095074e2feae2b0bedcb795e4a4e56190574efafe863d0f71853c883aefc32f3edbc3b9f24a6ad6bf9585a3f75017f5775eb9a6cf8f0f57cf3f65baa7671fdf2e67eb64fbf87bb07af1188d4f62a9fdc211fb0d38357e7a5c5cbdbb05dd82fdc6fd93b6b86725b7efe1e7bb166cd27efd4a595eb9360e51da26911fc351ce383b3e4bcfe780d92bd7773fff09e9e7a88c5efa6becf3705f0cde13b9b3dabdbe3ecfe7e71457de537ea327654ad97e3eb7f78dec3f5a7f66f69f11c647ddefe7ff56a6a46fcf87a4854863be3f725cd0c9d8f7c7bdb54b783ef53def9c3ff00748824e265bfb3a619b583add4f60f7b0a5cf7a927a2715afebdf7304e22292d3dd04778e2b3cd06e72a7eb8ade8365e31c4855be8eb8d4e9e2e0f56298aef7fae75f35c46b99ec78edf6970f665c44e4bad84616b466012ad3e5ffe19eb97d741fc4b1ca9fecfb1faaeb05a83b3e2bed3f67bf3947c74fb55dc623d62df536f3ba932e6d1ed96fa97f7afc5c3effa373f97f5cb4c5f93e8bfe69fb07c499d463b6ced9f1790c9ecf39de59fb1fdaa7fc75ff46e5dbf34fcfef3176fe6dfe8ed35f679f548f7e6ecea3db57b2a3fd93f93945bef4e4b929c243cfd399fd03bdf567cdfe738248dcca4feb4778aeaaf2a40edd77556740b5bd49db487354fa306aab6e43a6b1fd7be60ffc1174840ebe17110c7b162bf8ed3841e61cee5de744132104f833c8b3cbfc871f0641f7bde828df776f7b0017c74740eef9cdbc885eaf467c0000e07520e8000000006e0e820e000000e0e620e8000000006e0e820e000000e0e620e8000000006e0e82ee7af8cd42e52f1e65cf34231de02f30dbd70b00001459d0191b27d6db6a589b5b1e5b70e3e68913c1f253fbfa149b87fa0d38ebf483fd8ea7158cb72a91cd3fdd3d6e3c7a751caeff9b98d9efa730378616be41385bcf8e67718b8f94ff2ab63c8adff8d58fc34cacc91a619d62b29a1f00e08fa12374e53762d915bd140ef5376671dcfb1d4cdc80518eb61a395211983f11a14a474059692f406c3ab4598ccc89addfe1bcf551416fe1cdf63b4bd3ff17474247f6ad9f9f24f0f53db3fc571774c3f9557f49719f7bfd696c25ecc80f00f0e7e80b3ac7c0d9990bee0259044e1ca94453dac55a44a68a7438672867f56d51bcf9d1475f721661ce5f3a9fe9d12c2232e375bbef125d4bf95cd9c5d15df19ee8c42dc163d6af6d34a87fda3fa9375d5714362e8e0692b2ca76a6f649ddfed832b94fd531b75f381f35a4b979d09c8b3919df49fb564882c3160cf3f615f3cbfd3f0bb205fb6e758628ec56ee7a7e7fa0784aafe6ef99f15b793e86e52fb45fc6b688b84b1e730d70f518d1b9f5fc00007f90aea0f387c7772202b2907644c9085f7e5a80878b7158d00b67e20862508b23b96f5be49b57b4de016dedf44e4b9c54bc47ca6b9c7aec5b71ada2b09322441eb5fd82f32cef2bdbdc10a3108df352f4ea5fe9df2882f2e99c7931a6623fe7b4f53d41548671f487b0bb7bb6c3d8e5f3c87e8fe25e1118b5c31f8eef4afb2614277534361cb72fd87d3bbb535e8bd7af4c6711b62c763a369ae7ef8fefd9f19bcd9f95f247ed6fe67467ae043bd763b39e1f00e04f520bbaec70dca25f38d78c5bc4cd455b9c6fcaabd9eeb52238d6c2ed17eae61bbad43b58bc258f2110b51390fe1502358aa7fc59587012a6c3b1f259e53bece86324e6699c97a2e7f056fad775b8dee6edf834874477fa9419d9cfa515119ec20e0be35bb42bd1171016a9ffbd31ecb74f9036ea7457563546334116eaecf77596bf3bbe2f18bf57947f5ed0c93a629781a0030018d08bd0f91fe5378bf3206ab717598c7b113aef3c6c41575e5374cabb93a0335f591a7d32eb8fd767fdeb3bdc89a04a74fa94e9da4f1cb51b0bf525a16cef647c57db3761d8ff61fb2a1e0f97be3f4237b49de3b0a07bc1f8bda2fcb1a0abe6bdf1cc8e6cb4921f00e0cf32fa0d9d7cd682a25d6c4f38d9e1622cceb5750cb2a017ed93573e4a1cadbc72ed3bac4857906cf49c4e51ffc157aea9eca62fc63dd6f559ff74b9fe2f06957016f12baf14f5fd0d0341e0e9d9cf5f7775a5bae3efd5747b67e3bbd4be095dc1b1d0bef06546b54f6c51cde1917dad71abdb33cb3f1adfb3e3f78af247ed171b3fabf28afadc17c65e74ce33cd0f00f087c982ce2dde392a941d721025e9b319419a881f0b5d4ecfb9d8512cd71eb7886f753be751bc162ed3455824e720ce2a5df7e5aafea636f8bec66b99ecb0458819e985030a02c45f17472d0eadee5f143cd9c955842884d4a51ca1675cff4aff042f42e275fd7ba9c0c33bc9944fec2b8225b4c3ae5f8fd1d87e651be507f51ffef3260056c6b7dfbe3965fb74bd8159fb447c3d8bfa6bfbf5edabcb2e09e3b7277f7f7c47f6198fdff9f26339c3f925f5d8f332a489602cafd58cf20300fc697484ee5288435162e08e8820a89d4e106ce57d1a1fb111d177f3be03ec631c95060080099715748e3b2ef03a0ad46cab3189ce010000001ce2ca820e000000001640d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d0fd3dd81a050000e09791059dda48b4b7ed86b539eab1cd3de326a71341f153db96149ba7fed3ee465f9f68f06a66e577dbb7b42d8ab3bddc231bc00efb10c7c8dd6ba7ff1c3f6dffb7503f7f93cda7cfb4ef47fa777716d6c78db5f50d00e0a5e8085db953bbec0a5f2efcf54eee222c469be4dac40d443f2611a29fda58d88ba2fa94860b31699f8cd9704c62644ec6723e7641fcd969f7a47bf4d74590678acd75afc96c7d4cd797d637008057d317748ec16bb9e6de45b2081c942df48efe2aa2534e6c3c8b28defce8afaf677534512e3b88a19437a3058dfa966ef75d0450cae7ca8ec7142ddb6952feb47d82177c76e4c0ccafc7209e9d1ad2649c2a4157a48b2deb7a2afbbbff4b5b74fa68fc52fba4effed8347f8f1aa3817dc286ceaeac942fe6cd7343ec92ae2b8a39361ddf73f36b85a1a09bb52f9e3f1bee71e3579fbbbab77feefff5f8bdabff7a6efab6a9b6e6393e997fd3f933eddf9895f571757d030078395d41e70f97ef447106a261842f3f2d72c305cf2ddcc621dd61b1d40baadcb739c0e615ad77005b3bbdd391053e0b886a811662df8a6b15cdc21e09dfccb5fda273aaee9bd12bdf336d5f699306e728a5ec563888ad36db64f1a5eafa74ceba1873b1af73aae97368f776b6a7bcd6d5f967e3e7f18e3ccc137f50bebb271d989fe8db3f3a79d587f6f0fb765ed50cc757b7e5c8fc9ad08e4b4b7f7e3c0a5b89c0b19edf5efe70bd3f7eefee7fd32e99ab6a8d98cd3fcf60feccfa37a3689fb13efaf4a5f50d00e00dd4822e7d7b0d111a6b319645dc728ae29c535ecd76af152132177c590cadc3e9478b6f6701d50e52fa5747649afa8f0a3a2b5f51fedc3e09b3fcc442fbece86624b6a9110e335bc8ffa7ed9731dad2e4d5f0261027e397b0da51d1b34f23381cf5981f1674d2ff57ccaf09a7049d6b6369ff7d826e387edfd2ff87ab7f7bee8bf15c9a7f8e619da3f93947fab7d5dbae8fcbeb1b00c03be845e8fc8fe61be73788daeda5e320725a47d095d714af723852ce4478980ed1ca77c0a10b66f98985f6f5049de570f22bd7a92d160559e2f170fdd01190c9f825166cd6b3cf6f14744d99f15a5baeb3af443b95c8b0f2a6ebd376d5e3f74dfdcf6548dea2bec5f9b75a67333fe768bbd9eba3a2632f0080b731fa0d9d7cd6bfb1aad3773b79cd70c113e7d42e9622488af6f9573e9b78691cbaf14a68ea700a1163d3da21504614aef9ca3595dd8a1fb1958e583cbcb3d60e4f3ecb2bab2d4f4910fb6a7c2aa73c1b3fcf8243eedbdf953f7de5aa0588ababf9e2302aff05f36bc26141e7e785eb4bea5bfc3d5d9dd7a70dfa371abfefe87f5a539aba1cb3f9e719d439ebdf8cda6ef2b9fb875b083a00f86eb2a0738b5b8eda64271e4449fa6c4678f2bdebe872fa8bafe58c5c7b4464e4badda25fbcf628d345f82467ee17df78dd97abfa9bdae0fb1aaf65f2a2ec1c4d9de6d1822108147f5d848408866587362f7fdcbe48147cbd4898d8deffb183216482834e658bf38b6d52f3c18bbc54b7b3bf38ad548e88a76791ae0562c8df1f3fbbffdb1c58b18f94a7fad0d41f9d78ca5ba4cfcb3f3bbf86e8e7af22d860de3edd0611b61ffe7312462bf6db377e2fedbfa22f941e83f967f76f9b3f2bfd1ba0c7473d0f7a7d4cacac6f00002f4747e82e852ca0e6a27e1fc4a17ef7821e049b9d26f888d52f8d1e58511d0000803fc165059de38e0e5a47d1da6d3ddecc243af79b29a29755c4040000e0d773654107000000000b20e8000000006e0e820e000000e0e620e8000000006e0e820e000000e0e620e8fe1ebf79eb120000803f491674c6c6a6f5b61b7af3d07ccfa17dd6e226a01341f153db96149b97fa0d76ebf4f76e183a2bbfdbbea56d4b9cede51e37debd3adeddbf35e21ce96e41324bbf1e723286df6cfacbb6afb57174799f8c5d4a73e3be7b8c56f347dbee14fcd3f6cbfc2cd2b74d8dd77804fba5b2abf5696ebf1913fbccda7fba7fd47faafe597e80df8e8ed08960db1640b778568ebd4c0fc262b489ad8decae2e3bfabb8773e4307e6a63615934be8c5314aec2a47db38d857d7e675719cbfd63f7ddc8023d126cb3f48bd211d33e72aa3ed7cf9b3c37f9e82a7fb4dcbe315ccbbff87c1accda9fe65efebc9350de96bfdeb87b5aff84a97d66ed3fd93fea3f57ff343fc06fa72fe81c830768ef6299c82270fa705ac24444a63eba47ceabd4513cf710ab74eb68a2af6775f4532e3b88a19437a305838a62da7d57df10a56c39fcbb7bafc1a4fc69fb04b1abbb66093e33bf1e8341fd21fae16cad2224d2c75d11d478be68a8c3cd83fa5c4eebe831ddbf497aea9fb43d4772f41817f9dd7dcdc6cfd5fc71ff175baea72fd2117435b540a991397dc66159f9579fcf159af6bfa04ccdec249699fd6634f699b5ffc5fda3fe9df55758f31be057d31574a36f38f2607544c3085f7e7a20870fa773bc46b83e381bb5808b831691114585b4b71018de816fed94faf5f98d525ee31062df8a6b15859d14befee21b622b8c56e895ef99b6afb449431413d2f7de3dfdfe451194f3491ff738cd87ca1b04d836bf64acb6b1917bbd78cf7d9da547bc280df3ccd7e5e640aaf3d389f962cecafc70fd499f43bfb7b369e5b5b42e7f96becc8aa09b39af83cf60c6c8effbb7f47c2e60e5976b510c7b8af15c253c5741504fec73b6fdb57d67ed7f49ff22d4bfbffefade3a3fc06fa71674f961f11110cbe944c76a5dd70f5b66bbd78a10998ecd3fb8f56b45a977e03c258fb1806be122fd2b04aae558e342505cab088e7d215f51fedc3e09b3fcc442fbece86624b6e998a06bf334361de1da5e44c8743b6763b13a56d67d82dc5bd83da1ed2f736c4b9357dba5c398a52fd26ba36268576fc776de2cd3c9bffc7c2eb0342f644c8e8a2ef9c2e4e670af7dbbe665cdaa7d67ed3fda3fea3f57ff6a7e80df462f42e77f34df3c1483a8dd5e460fbba475045df71b57a7bceb08ba75ccf2130bedeb09bae92bd748affe73824e04adcbafbe2414796763b13a565d9b87f9d35eefe004c3435e99f7f2ccd2474ce7453ff22975ca3c3f1a7958ce2fb6ed3d9f535623b73bc7a4a6fb2cacd6dfb2cfbeb3f6efef1ff59fab7f5f7e805fc6e83774f2593bfcd6d1ef7f60334387e1ca35225722488af6c5df442551b0f2caf56d82ce51d47fd157aea96c4b9cd5f7d4d74fbd72f5ed76223de67dc4dfd36de32163e5ca52651f7de5dab39ddcaf7f545f13beac6c79fd971a354767e9cbcc049df96c385bbbf6ebbfdc6be673bc2f4482eb7159cd1fe93e9fbdf2159dbc62bffa8f1ada2f14fdf24bfbcbf8db5f48fa6d1f31b7cfacfde7fa47fde7eadf39bf017e2359d0392793a336d949ba87c439ddf4d98cf00cc5858d2ea7e7d8ec28537868b7bacb884f9d2ec2272d1af270a7ebbe5cd5dfd406dfd7782d93170c114a467ab13089e888d7457cc8b7c54eff5ae6e58fdb17f1c2a91fc914dbfb3f2670e596f7acd42ff6aefad81185167a0c4418fa05598bcf28d0c33db218c736a539364c8fffaf28e7d0c38bba9c2e0ec2d92fd941c4e9b3482ffb374b9fb1fafc988e48c6b5ceeb689f9164876ddcf6e59f3d9f9df2157d475a3d9fcd1fa50883f2e36bd62dbf1d8939e4c897ec336bff89fe51ffb9fa77cc6f805f8b8ed05d0a115cb558b9192258d605dd6b0882cd4e137c444d16bf03b6f5826e8780010000806fe2b282ce714701a1a368f637d43722426d109d3b43111d34a24a000000f0835c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b839083a0080d723fbe17df7b64500f0872904ddc7b639a3de917b1539b9c16fa6fb25f9db85ccda18b7bcefa3dc3876f762b89a3f6e6eb9732fb669fb9bcd2d8dcd51873c82fd52d99d6d4fe49405b1b1afe34ddb94bc056333e73b227bfded6d7fda54f9cc46a7e6c6d2c237ecd798dadfb0b885cd2bfaffd3ccd6b7125963eacdbbc50e21bfac4f777e0600e082684127bbe88705f7e184d15e31a2e81c6d541f13557f83957de7f2d13efee8ac7d3bbeafe50fd73f4514ed1674e3f67b4177c2b986f2b6fcd6c6c4e2109eee9eedf8adfbd1d8ed1751cf118df4fbaca069ca3f39e76a66ed6fe67b25e8deddff77336a7fa6b3be69cc395ee7739faf6e0f00b811af8cd06516163c61b6789e750056fe2c5a5fe008dfed5c1b41e7ca7f9e2abf3a3ac739e3d4fee6b8273786e9738ec214476fb9fbaa08622a43f2e748a331c6a6b313e2f9aea17c374e6a0e86e89413e22a822965971b4fd74703b97a16234829faa5db956d92ca503669da2f631fd3347afef9f9f8a9ee5b110f1569ced982a16fbfc0c03e8bed0f75ca3ca8ecfa8afe9f985f5276c853ce8fe5f217da9f99ae6f76744ed69e62be4a9d2f5c2f00e08f53ff86eef1708ba53806bfd8cba2d62e4c535604dd6c319374e73476d79d30f27b8794ea3cbb985af9e59a7608e2507647d2248218f237e58b5d9d13ca4e6967f93e32593894e098938d1a91e0ebdbdaf0e99c65311e92dfb5277f16bce80976f65144774f1d4d6ceac93c8a7bc5816b872a76f14e38df23b6da9c7628773bdf530ecf5f3ed5a2331f1a27ece8b77f135c569ae4f3afe1637952f6f439a9d0af5ddbbc63fbadd867defe34f76cbb9ee9ffd9f9e5cb57cfc491f247edcfc8733118b760e736dd1474abf3130060861674290a22df723fddc2ffe1c45d71f32a93054f9045cffcf62bb885eed42bdf4efe1c7151ccdad963d8fe4447242c110f22d7ed0b0e51b7d9ddf3b9287a97044b78d59eca2b04a0e4af6c17a8ecbc38f6e63d7edcb6b2e50b4529e86c71b5dd230255e7df9cfb0ae2cca5effe39f04257ecd1dab7db7ec74cd0147366c15635a97cb30d13fbadd867d6fe50a794734cd075fbff82f9f58af247edcf0cc7cdd9a66e73044107006fa58ed009b23086c56e41b4584c1d551959d1f81f0dcbb76f236d85e5fcb2981e155b83f697f41ddf12d582efc7a52aaf71123d3afdadf367a7286358dcbfd897e9d8873ada7bc411bab6a4df40c6fbf4fc9b0b3a8513c432179623748e608bd04fff7f793d68d8cc6e7fe0b0a059a45ffedc7e051dfbccda3f6beff1fe9f9f5faf287fe9b99eb4a19756474ccfad41000015ada013c71017b5a30bcecc5199e5868894fecb54db213d5cfbda6fd6ebf923ddbef5ca5774f28ae0a8ffa8a1fd0bc47ef9923fff5187bbcf47148bfc2224c529c47ba2f85815bf8d2032f3876b9678129133fd6de56cec1da6d3139b3a679a5f9fc5df83e9f19bbd722ded2765b8b69863dcc1ddff94df6049db24af2fafbd6fe4b4b5ddfc970b37cec9becd7c5cb0554d57702cd96f6e9f59fbebf6d6ed39d3ffb3f3eb15e58fda9fe9b6419eedf1baa1d727698f35bf00000ef1ca089df54ad38a90340baf200ea9ceeb68eb17c12969d5c2b99cbf6c67bb3077ca5798edf7045199cb16f1d1dc33283fbe66ddf21b91462fc242fafedfe855e5bbb1b1fae8c7df14420fef8452fe7f9cf3dbda98fa55b2d9c94ed776d8e65db09d17c412758a7df4cef6a3df7f1117f2dbcf5cf65efbf83994ea13e7bcd5bdd27ec18ba494a6ead77df33671f7a5cf3d8152a37f3fa7ed9298db6f6e9f95f697bcb2ffc7e7d7f9f263399df60bb3f54dda301b4b1f158d7957c71d0060094bd0015c111d3d01b816122d667e02c00f82a0833b5044a78ca82f0000c09f064107000000707310740000000037074107000000707310740000000037074107000000707310747f0fbf19acecb966ee3507000000b7230b3ab51167a2de18d7da5cf4d8e6987193d089a0f8a97d9d8acd47ff693711960d46dfb929e8acfc6efb44a47dcd4e8e908d79dd3db281ea8536364d73abb6f5fb8873902d500000e037a02374e54ee7b2ab7a292cea9dd04558ec77c07103ce8f49844804e64f4490bc28328efbb90a93f6c9980dc72446e6642cbf4f3cadf18a36758fc63209e2d64e030000b8117d41e718bc966bee5d248bc041d94238b3b4be2e22531fdd23e755ea285eff682b69afbf266775e6fca5f39f1ded333fae490442cae7ca8ec7fc2cdb6952feb47d82177c7694cecc1fc760c53e23fbae51e577ff973252ba177472207e4cd7f5a7f6a53991fa92ed24fd4ef914c51cd2c7a6f9c8662de8fafd4b1b1beb71c9f6147b2fd90f0000e04d74059d9c2beac492193119888611befc24e286824e5e87b5ce3088412574c44117675556af68abc3e7bdd315271bef91f21ae114fb565cab28eca40891476dbf5200acd22bdf336d5f699386f8aa55fa5edf33b3cfccbe3342bfb6b333e5b5af16a4be7e19f74efd925eccc7d897fcd9d18fd0495bb7b2f3970355ffb07f9df9aaed38b31f0000c0dba8059d8f2c782af194898ed1ba9ef36ab67bad0891e9f0c479ba7ca55008ceb5b84fb3e8706782e0b0a0b3f215e5cfed9330cb4f2cb4cf8e6e462682ae6b9f05fbce9131dcfa2eaf8e3781351f9f95f1eb0a3ae3dec2960bfd93b2654efa689dcbf770a2b0fec2309d5f000000efa017a1f33f9a6f9ce3206ab7978e03cd691d41575e532c38e42587ab9d7c076da78c95efa04337cb4f2cb4af27e846af5c85a17d16ecbb8bc7c3d5d746e846e3b3327eef1474e1ff611efaffcbebe155fb010000bc93d16fe8e473edf04b0735899a8de838d08044b35ac72c82a4689f77ae9b785979e53a75b80b82a9b543a0a8ffa2af5c53d98dad62dac83e33fbce085f06b6f2fc97861d8248dbe5217f542351beca4eba8d2218b72f06d2561d117c7851b6fcca5570ed79ca6fe4a44e699befcf76ffacfd0000006f230b3ae77c72d4263bb9204ad26733c2a31ce22aba9c9ec3b3a34cae3de28473ddce0117af85cb74113e49148ab34dd77db9aabfa90dbeaff15a260b0e114a467a213c4500c4eb221e44502c3bf479f9e3f645a2e0eb892cb1bdff63802a02ba629f917d5790e8d9b318bf4d602dd51f057cce6bfcd1891789319f2ebfc99ffe28c2dfa7e6fba87f62db2c961f85705eb31f0000c09bd011ba4b210eb11bc1bb071fcec97fb7330f82cd4e13fc2bc96174140000006ec765059dc37a2d78757414adde98f9ed4ca273000000f04bb9b2a003000000800510740000000037074107000000707310740000000037074107000000707310747f0fb62e010000f8656441a73642ed6dbba1374fcdf71cda672d6ee83a11143fb56d893f4120f7b1dafddfa7bf77b3d859f9ddf62d6d5b229bebba7b6403de37f5e1ddf6010000800a1da113c1b639e247e398cbf4202c469bd8dac8e91372c2c32442f4531b0b7b51549f237b2126ed9b6d2cecf33bbbca58ee1fbbf374cf5a05000080e3f4059d63f05aaeb977912c0207650bbda3bf8ae8d4d7e7bfcf228a373ffaeb4bcee2ccf94b71216228e5cdb83252fafc38a7f9d1544326e54fdb2778c16747e9ccfc7a0c8aa3b1c456aa1c29375ef7e392dbaaec3f6abfcaaff90951090000f0ebe80a3a7fb87c278a33100d237cf949400c059dbc926d2339410c2aa1200244098ad9e1ea5ed489d08af74879a6f0a84552454fcc86c8a3b6df0e31a7e895ef99b6afb44983135d52b6f4bdbee7d389dd624cc57e4ed46d9f45942941ae5ff72a46ed2742070000f0066a41b7454f2af1941191643965111265f425b0dd6b45884cc7efc542fd5a3188b3e23e8de43104a2162ed2bf42804471933f0b53c1d4112c56bea2fcb97d12e7045d2fba19e9093a6ff385f6399117eeeb8b46041d0000c037d38bd0f91fcd37ce7710b5db4b4780e5b48ea0eb46053be55d47d0adf32e41377ee53a11cc11991722e6befc3cb0db88a0030000f86646bfa193cffa3756ada35e1301261d0116906856ebf8459014ed8bbff94ae265e595ebdb049da3a8ffa2af5c53d98dad1ce115746f4c42deed9575b0bdd5ce51fb75bd8f4f67ff46b8030000c06eb2a0f3919718b5c982218892f479e947f90be872fa8edf8a32b9f638d1b1d5edc44111252ad345f82451e8c548bceecb55fd4d6df07d8dd7323a8255a779b4f00c22c75f17e12382a5d3bf9679f9e3f645a2e0eb8924b1fdf343eab284d4c38bba5cb6b3af886eb96f6ebf15fbb8724444a7b42c0e010000e0143a4277294430742378f7e06310a97a1741b0d969029b0a030000fc422e2be81cd66bc1aba3a368f5c6cc6f67129d030000805fca95051d000000002c80a003000000b839083a000000809b83a003000000b839083a000000809b83a0bb1e6c2d0230dea01a00002ab2a0d31b0b77b6ddd09bcbe67b0e2db87113da8960f9a96d4b8acd758d03e8eb132b9659da5644362776f7c806bc9d3a0ed7ff4dccecf753981b330bdf209cad67c7e3c6dababf26e5bf8a2d8fe24f07f1e330136bf6e6d7ebf90100fe183a42577e239653034ae1507f6316c7bddfc1c463a73e2611a89fda58d88baef71d4735dbf8d7d7effa2db67e87f37efb59aa6fb6df599afebf38123ab26ffdfc2481afef99e5bfbaa01bceaffa4b8afbdceb4f632b61477e00803f475fd03906cece5c7017c82270e2487b477f9547537dfefb2ca278f3a3bfbe9ece29e4fca5f311b195f266b4c3159119afdb7d97e85acae7cafe0cf7373675655a82c7ac5fdb6850ffb47f526fbaae286c1ccfc64d69758436b54feaf6c79ac97daa8eb9fd64bc529a9b07cdb9b193f19db46f8524386cc1306f5f31bfdcffb3205bb0ef566788c26ee5aee77f7eaafbaaf97b66fc569e8f61f90bed97b12d22ee92c75c035c3d46746e3d3f00c01fa42be8fce1f29d88802ca41d5132c2979f16e0e1621c16f4c299388218d4e248eedb16f9e615ad77405b3bbdd3122715ef91f21aa71efb565cab28eca40891476dbfe03ccbfbca3637c42844e3bc14bdfa57fa378aa07c3a675e8ca9d8cf396d7d4f1095611c1f528fbbc7ff9bf38cecf728ee1581513bfce1f8aeb46f4271924763c371fb82ddc3d9b6feb3d8a2eaeb2cc296c54ec746f3fcfdf13d3b7eb3f9b352fea8fdcd9ceecc9560e77a6cd6f30300fc496a41971d8e5bf40be79a718bb8b9688bf34d7935dbbd5604c75ab8fd42dd7c43977a078bb7e43104a27602d2bf42a046f1943f0b0b4ec27438563eab7c871d7d8cc43c8df352f41cde4affba0ed7dbbc1d9ffa70fd5e9f3223fbb9b422c253d861617c8b7625fa02c222f5bf3786fdf609d2469deecaaac66826c8429dfdbecef277c7f705e3f78af2cf0b3a5947ec3210740000037a113affa3fc66711e44edf6228b712f42e79d872de8ca6b8a4e79771274e62b4ba34f66fdf1faac7f7d873b1154894e9f325dfb89a37663a1be2494ed9d8cef6afb260cfb3f6c5fc5e3e1d2f747e886b6731c16742f18bf57943f1674d5bc379ed9918d56f20300fc5946bfa193cf5a50b48bed09273b5c8cc5b9b68e4116f4a27df2ca4789a39557ae7d8715e90a928d9ed329ea3ff8ca3595ddf4c5b8c7ba3eeb9f2ed7ffc5a012ce227ee595a2bebf6120083c3dfbf9ebaeae5477fcbd9a6eef6c7c97da37a12b3816da17becca8f6892daa393cb2af356e757b66f947e37b76fc5e51fea8fd62e367555e519ffbc2d88bce79a6f90100fe3059d0b9c53b4785b2430ea2247d36234813f163a1cbe939173b8ae5dae316f1ad6ee73c8ad7c265ba088be41cc459a5ebbe5cd5dfd406dfd7782d931db6083123bd70404180f8ebe2a8c5a1d5fd8b82273bb98a108590ba9423f48ceb5fe99fe04548bcae7f2f1578782799f2897d45b08476d8f5eb311adbaf6ca3fca0fec37fde04c0caf8f6db37a76c9fae37306b9f88af67517f6dbfbe7d75d92561fcf6e4ef8fefc83ee3f13b5f7e2c6738bfa41e7b5e8634118ce5b59a517e00803f8d8ed05d0a71284a0cdc111104b5d30982adbc4fe3233622fa6ede77807d8ca3d2000030e1b282ce71c7055e47819a6d3526d13900000080435c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908babf075ba3000000fc32b2a0531b89f6b6ddb036473db6b967dce47422287e6adb9262f3d47fdadde8eb130d5ecdacfc6efb96b64571b6977b6403d837f5e1ddf6010000800a1da12b776a975de14bc75cefe42ec262b449ae4ddc40f4631221faa98d85bd28aa4f69b81093f6cd362ef6f99d5d652cf78fdd79ba476f010000c071fa82ce31782dd7dcbb48168183b285ded15fe5d14372dea68ee2cd8ffefa7a564713e5b283184a7933ae8c946e1f87a491e857cae7ca8ec7142ddb6952feb47d82177c7694ceccafc7209e9d9ad28a08ad941baffb71c96d55f61fb55fe5d7fc84a8040000f87574059d3f5cbe13c519888611befc242086824e5ec9b6919c200695501001529cb559bda2350ee7d7e74b4a79a6f0a84552454fcc86c8a3b6df0e31a7e895ef99b6afb44983135d52b6f4bdbee7d389dd624cc57e4ed46d9f45942941ae5ff72a46ed2742070000f0066a41b7454f2af1941191643965111265f425b0dd6b45884cc7efc5827138fd48c8481e43206ae122fd2b04481437f9b330154c1dc162e52bca9fdb27714ed0f5a29b919ea0f3365f689f1379e1bebe6844d00100007c33bd089dffd17ce37c0751bbbd7404584eeb08ba6e54b053de7504dd3aef1274e357ae13c11c91792162eecbcf03bb8d083a0000806f66f41b3af9ac7f63d53aea351160d21160018966b58e5f0449d1bef89baf245e565eb9be4dd0398afa2ffaca3595ddd8ca115e41f7c624e4dd5e5907db5bed1cb55fd7fbf874f66f843b000000ec260b3a1f7989519b2c188228499f977e94bf802ea7eff8ad28936b8f131d5bdd4e1c1451a2325d844f12855e8cc4ebbe5cd5dfd406dfd7782da32358759a470bcf2072fc75113e22583afd6b99973f6e5f240abe9e4812db3f3fa42e4b483dbca8cb653bfb8ae896fbe6f65bb18f2b4744744acbe2100000004ea12374974204433782770f3e0691aa7711049b9d26b0a9300000c02fe4b282ce61bd16bc3a3a8a566fccfc7626d139000000f8a55c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908baebc1d62200e30daa0100a0220b3abdb17067db0dbdb96cbee7d0821b37a19d08969fdab6a4d85cd73880be3eb16299a56d45647362778f6cc0dba9e370fddfc4cc7e3f85b931b3f00dc2d97a763c6eacadfb6b52feabd8f20872b28bdf6cdb8dc37cfeda9b5ffbd345fc3822f600000a7484aefc462ca706940b6ffd8d591cf77e07138f9dfa9844a07e6a63612fbade771cd56ce35f5fbfebb7d8fa1dcefbed67a9bed97e6769faffe248e8c8bef5f39304bebe6796ffea826e697e0dbeac241a5b09753ef7f9ce021700e0a5f4059d63e0eccc0577812c02278eb477f4577934d5e7bfcf228a373ffaebebe99c42ce5f3a1f115b296f463b5c1199f1badd7789aea57caeeccf707f635357a62578ccfab58d06f54ffb27f5a6eb8ac2c6f16cdc9456476853fba46e1f6991fb541d73fbc978a534370f9a736327e33b69df0a4970d88261debe627eb9ff6741b660dfadce1085ddca5dcffffc54f755f3f7ccf8ad3c1fc3f257e657622ae85c3d46744ee64611b1973a5f28c601006e4d57d0f9c3e53b110159483ba264842f3f2dc0c3c5382ce88533710431a81c813818e778d322dfbca2f50e686ba7775ae2a4e23d525ee35862df8a6b15859d1421f2a8ed179c67795fd9e686e8ec1ae7a5e8d5bfd2bf5104e5d339f3624cc57ece69eb7b82a80ce3f8907adc3dfedf9c6764bf4771af080c3dbfa6e3bbd2be09c5491e8d0dc7ed0b760f67dbfacf628baaafb3085b163b1d1bcdf3f7c7f7ecf8cde6cf4af9af88d0053bb7e9cd33b1f0ac0200fc196a41971d8e5bf40be79a718bb8b9688bf34d7935dbbd5604c75cd865a16ebea14bbd83c5bb2310b51390fe1502d5722c47059d95afe3b8ece86324e6699c97a2e7f056fad775b8dee6edf8d487ebcf9cf1d07e2ead88f014765818dfa25d890501a148fdef8d61bf7d82b451a7bbb2aa319a09b25067bfafb3fcddf17dc1f8bda2fcf3824ed611bb0c041d00c0805e84ceff28bf599c0751bbbdc862dc8bd079e7610bbaf29aa253de9d049df9cad2e893597fbc3eeb5fdfe14e045562e88c1d5dfb89a37663a1be2494ed9d8cef6afb260cfb3f6c5fc5e3e1d2f747e886b6731c16742f18bf57947f56d08d6c54474c7bcf3c00c09f64f41b3af9ac0545bbd89e70b2c3c5589c6beb1864412fda27af7c94385a79e5da775891a382ce51d47ff0956b2abbe98b718f757dd63f5daeff8b41259c45fcca2b457d7fc3c0197b7af6f3d75d5da9eef87b35ddded9f82eb56f4257702cb42f7c9951ed135b547378645f6bdceaf6ccf28fc6f7ecf8bda2fc51fb33dd363cbad1398f1ba367d59ea2bd00007f992ce8dc229ba342d9210751923e9b11a489f8b1d0e5f49c8b1dc572ed718bf856b7731ec56be1325d8445720ee2acd2755faeea6f6a83ef6bbc96c90e5b8498915e38a02040fc7571d4e2d0eafe45c1d338b9488842485db5231cd7bfd23fc18b90785dff5e2af0f04e32e513fb8a6009edb0ebd76334b65fd946f941fd87ffbc098095f1edb76f4ed93e5d6f60d63e115fcfa2feda7e7dfbeab24bc2f8edc9df1fdf917dc6e377befc58ce607ecdd60f6943f3bc54f8a868cc3bbb1700e04fa1237497421c8a120377440441ed7482602befd3f8888d88be9bf71d601fe3a83400004cb8aca073dc7181d751a0665b8d49740e000000e01057167400000000b000820e000000e0e620e8000000006e0e820e000000e0e620e8000000006e0e82eeefc1d62800f76765df3e00f8436441a73612ed6dbb616d8e7a6c41899b9c4e04c54f6d5b526c9efa4fbb1bbdec25f7ce8574567eb77d4bdba238dbcb3db201ecb00f718cd4c6af57e1a7edff16eae7af19c76a63e06a5ca4cd723db5bb785697857bb97170da5c79b46f6281ea8369bf59fa80d0bf7b6c63f43df347e6c3b6f9f8caf89b1b7fab74bf176031fe6e8db8e0f30f001d7484aefcc6278b7bb930d5df0865f15f5eec337103d18f49844816ff6547f442bc28328e2bba0a93f6c9980dc72446e6642ce76317c49f9d764fba477f5d0479a686a2258a76eb3eb9569cbf1cefd5f78c08652a21128fafdbfb8cd7eb44cd2cbdc715f6a5bccafcb16cb832fe4dfbe37a20fff76b825eef658d46d001dc87bea073a887bde6e8a22c8b8e771083b205db913c7cfefccdd22d36cf6291afbe618ae38b79a5bdf95b67ce5f2e6ee95b6e815ed0a611061140299f2b3b1e53b46ca749f9d3f6097111b7049f995f8f413c3b35a4c9385582ae48175bd6f5d4dff0c5c1e8f68dc72fb54ffaee8f4df3f7a8311ad827441f5c59295fcc9be786d8255d5714736c3abee7e6d70a4154d9698277bad266d7d6ba8d3eaff43f8d699c0bfa9e2e93e731d0efbf66b63674d3e3f9b9a17c1117657bd217c1e2192b6c356edf7c7e0deabfd4fc71cf617334e0daf82741678d41b8b67e941e005c8caea0f3dfcea3f3483727e242b1f7c1f7e5ebc5a6eb40c282552f66b260e9c3d1bdc050af619a6ff0c6e1fcda094879cdc26b2c8235d662280487a3edd75bd8c7f4caf74cdb57daa4210a81563888ad36db64f1a5eafa74cea61873b1af1375e97368f7e610e4b5aece3f1b3f8f778a619ef883f2dd3de9c0fc44dffee210dd78ab3ed411899508cb707c755b8eccaf09edb868427f427d8fa26e21e5cd652ccce58c21106b66fd4f0ce7afa39ffe28c65a04985e7fbc6897fad23dbefe6d3c97da379c5fe3fa852bcc9f5ef92be31fbef84441d994f10875a6f4e2590280cb530bbaf4b0fb6fa8c5c1e88972112daee7bc9aed5e2b42642eecb21035df40c3e257dca7913c8640cc8b9bfbbff4affe46ddd4bfe004cd05d5ca57943fb74fa2b7607b16da278bb629c485d8266d177dbdb857d725ff9fb65fc6684bf3af7f721d93f14b58eda8e8d9a771988e7acc0f3b64e9ff2be6d784665c34529e6a83dcabebcb79e3b83d16e64a66d6d685fe2786f3d7d14d777594f3a7167483f15d6ddfa89f93fa859f9f3fb28ed86d5819ffd4fed91809f5173200b838bd089dffd17cb3780da2767be92c7039ad23e8ea6840e6550ba6b108d67417ec3adfc8790c182eb60bedeb093a4b50e757ae535b2c0ab2c4e3e1faa11dc264fc120b36ebd9e7370a3a5de670fcaabc3edfe77cae6466f36aa1ff09d37e0a3b5d848a2b4b7d89acedf95e4137af5ff8e9f963961d5919ff95f66f884df6dc0f003fcae83774f2593b8c7631d9e9e4359d052e602f24e2d08af6f957169b7869167ce395c66cc19c3a36476b874051ff455fb9a6b25be728b67236cfd71ede41d4af4ce595ea96a744cad4515dffa5408df16cfc3c0b22a86f7f57fef495ab767aaeaee68bc3a8fc17ccaf09b500d9ca2ceb0a94fd2bf306dbee89b084f155edf5fddbfa34eb7fa267bf8499eee7b51b8b3436f1f76cda9efe75a1d4a7eed15f3a97dad71b9385fa859f9d3f8fa1c05a19ffbea073f7578276bc4603c0e5c882ce2d1ef95b7f5e048228499fcd0841b560aca0cbe92dfc7694c9b5c72d5a5bddd50254a58bf0498b972c96e9ba2f57f537b5c1f7355ecbe4054d163c23bd5860e3222ad765e19405bfd3bf9679f9e3f645a2e0ab9d4c426cefffd8c17044c1c1a4b2c5b9c436a9f9204e23d7edec2f0b7e2a479cc5b348776dd70e6c387e76ffb739b0621f294ff5a1a95f1cb538612b7d5efed9f935443f7f153ed2923f27a7addbebc641cd8d5c9f94b9f3f90c222596e5ecb3fa7ccded37b7afb661da3225f537ac1932beca1693f9b5d23ebdc68ceacff7fce0fc917b7a73c95c53abf12fd78fb25fa1fdee992f9e4fdd7600b83c3a4277296431baf9b74371084bcefc8504c166a709fe1bfa2ffde6dd4440007e0d4e0c32bf0160c465059de38e0b98fe16ec5fff19f7bc8d4974ee3753441f5454020000e04f70654107000000000b20e8000000006e0e820e000000e0e620e8000000006e0e820e000000e0e620e8fe1ebf79eb120000803f4916746a23cb44bded86defc32df73689fb5b809e74450fcd4b625c5e6b9b2d966b5af9becf5f6cefde566e577dbb7b46d89b3bddc231ba476ea7877ffd68873a4bb05c92cfd7ac8c9187eb3e92fdbbed6c6d1e57d327629cd8dfbee315acd1f6dbb53f0cfdb9f3856fecc7e7326fd97e747b5bddc1478217dca5faf5f7c48183fa9ffe7d718805f868ed0953b913f1ac75eef542ec262b489ad4ddc2053767c1f2de83fb5b1b02c5a5fc6290a5761d2bed9c6c23ebfb3ab8ce5feb1fb6ec4018d04db2cfda274c4747d2c53fdbcc973938f56f347cbed1bc3b5fc8bcfa7c1acfd81e3e567065f46464cfb1f9f8dfcb966963ee1afd7df8c9bfbbc67fe02c084bea0730c1e607bb19e9345e06471906ffbedc32e22531f4d23e72dea289e5ba454ba75b4ced7d32d2a397fe980440ca5bc192d184464c6eb76dfd5376029db7d1beddf6b30297fda3e41eceaae5982cfccafc760507f88be385bd74743ed89a0c6f331431d6e1ed4e7c25a478fe9fe4dd253ffa4ed3e9213db98c7b8c8efee6b367eaee68ffbbfd8723d7d91dab175a805528dcce9330ed1cabffa7cae60b5ff25e52fda6f46d3ff599b5e6013cd5fab5fc6be582f5edc1f803f4f57d08d2200f2207644c3085f7e7a80870fb373bc46383f3803b5908b831691111709696fb96004879fda29f57b071fef91f21ac710fb565cab28eca4f0f517df805b61b442af7ccfb47da54d1aa2336c165745bf7f5104e57cd2c7b1e82879a8bc627f2dda65acb6b1917ba58d9b609da547bc280df3ccd7e5e640aaf3d389f962cecafc70fd499f43bfb7b369e5b5b42e7f96becc8a209939bb83cf60c6c8effbb7f47c2e60e47f59f92bf69b61d94fae45b1ee29e6db42fa1efe60fdcd9a13dba0ef018013d4822e3fac3e02622d9ad1b15ad7f5c39ed9eeb52244e6c2ec178efab5a2d43b78f8258fe120f42222fd2b04aae518161699e0d817f215e5cfed9330cb4f2cb4cf8e6e46629b8e09ba364f63d311aeed45844cb7733616ab6365dd27c8bd85dd13dafe32c7b63479b55d3aac59fa22bd362a8676f5766ce7cd329dfccbcfe70256fb5f56fe82fd86acda4fe6cc4874ced27bfcd1fa1174006fa617a1f33f9a6f1efa41d46e2fa3c540d23a82aebca6e894771d41b78e597e62a17d3d41673954eb778abdfacf093a11b42ebffa9250e49d8dc5ea58756d1ee64f7bbdc3e3e1da3788c0cdd2474ce7453ff2e97f54eec6acfb1c4c58ce2fb6ed3d9f531622b767ca3ff85c09fbec379b333be794e32fd75f46e41da7e61800348c7e43279fb5c36f1dfdfe0525337c985db946e44a1684a27df1375169915879e55a2c28ab22a1a2b543a0a8ffa2af5c53d99638abefa9af9f7ae5eadbed447accfb88bfa7dbc643c6ca95a5ca1631be09a6597a64e0ece57e79656aa509e1cbca96d77fa951737496becc4c9098cf86b3b56bbffecbc4663ec7fb4224b81e97d5fc91eef3d92b5fd1cdab3853fecc7e26f3fecbf8eaf9f1e1d2f5fa374b0f1cb7ffefaedfe1c65cd72fcf6377fe01c07eb2a0738b648eda6427e91601e774d36733c233141736ba9cdec26c4799c2a2b4d55d467cea74113e695191c52b5df7e5aafea636f8bec66b99bc60895032d28b854b4447bc2ee243be0d77fad7322f7fdcbe88384ad7efde37f0f02d59ea3222a075d91e5dbfd8bbea6347145ae8311061e81d82169f51a0877b64b18f6d4a736c981eff5f51cea1877722395d1c94b35fb28388d367915ef66f963e63f5f9a91dad47c6b5ceeb689f9164876ddcf6e59f3d9f9df21566fb1547cb5fb59fc952ffabf563fa4733d67376c6febfb8fe88440873fee5b5110096d011ba4b2182abf9f6772f44b07cf7a215049b9d26f8889a2cee076ceb05dd0e0103000000dfc465059de38e024247d1ec6fb06f4484da203a7786223ab81a1501000080efe1ca820e000000001640d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d00100ec47f6db632f3500b80c85a0fbd8369fd43b82af222737f8cd74bfec4d23ad8d71cbfb3eca8d63772f96abf9e3e6973bf7629bb73f71ac7cc11fcde3ebb9a1b330366bbe23b297dfdef6a74d93477b00ce30378e16be613f46bde973c1e21635afe8ff15587ffee419af37e7bef9f30b00f7460bbaed2896871346f64edf4b748ee6a98f89aabfe1cabe73f968257f74d678c7f99ab5fce1faa788d7dd826edcfec0f1f21bbbb9cf777490b65d7e07f51cd048bfcf8e5753fec14da07bccda5f8c5bdcd750dff3eefebf9b51fbf73c7fe61cff25cf2f00dc945746e832f5c2d661b8b83ace3a082b7f16ad2f709456fbcf942f798b8d947797511dcde39c716a5f73dc921ba3f43947618aa3b5dc7dd5c6c8a90cc9ef23b13e6f6b03d3d909f1fcd650beb3939a63213ae5fa9fca8d65971b4b57fd73ffaf05478f14fdd2edca364965289b34ed97b188691a3dbffc7cfb54f74de6b7459a53b660e8db2f30b0cf62fb439d320f2abbbea2ff27e697941df294f363b9fc85f6af3f7f7674eefcf30b007082fa37748f875b4cc5717867208b5ebb704d591174b3c54ed29d53d95d77c2c8ef1d56aaf3ec626be43f5bbee9106ac73ac047068bfcc131271b6c0e3ba6cb38a9367e3a6759d85bf23ba7983f0b5ef4847efa83f6dd3ddb61fd81a69ecca3b8571cb876a822babc13cef748b47373daa1dcedec55391c7fd7599ec678343677f4dbbf092e2b4df2f9332c637952f6f439a8d0af5ddbbc63fbadd867defe587fc7ae67fa7f767ef9f2a5fe13e58fdabffafc053bb7e3ba9a1f00e02d684197a224f22df8d339860f27ee8a9b5711a1307164b2286a6754e016c253af7c3bf9734446316b670fabfd67cb3fe510e4dea96009afd293d32b04a0e4afda1ea8ecb838b6e63d7e5cb6b2e50b4329e8aafe3b4a3b8b40d5f937e7be823873e9bb9fe7ceae0f6f8ff64b43b7fd8e99a029e6c482ad6a52f9661b26f65bb1cfacfda14e29c79e7787fbff82f9f58af247ed5f7bfe9c6dea3647107400f0a3d4113a4116ceb018b6a26589a9232b232f1affa362f9766ea4adb09c5f165b4300add16f7fe640f975c46557199d7b6b27939da28c51717fdf89174cc736d4d1de238ed0b525fdc631dea7fb3b17740af76543c67a3942e708b608fdf4ff97d78386cdecf6070e0b9a45fae5cfed57d0b1cfacfdb3f61eeffff9f9f58af247ed5f79fe46363af5fc02009ca51574e238e2a27774419a3932b3dcf0fb1ffd97a9b6c37ab8f6b5dfbcd7f347ba7deb95af58b1cb91f25d1edd7e111dddf61bcc5eb906c2354b3c497dd3df4e2e8814d3e9893d9c33cdafcfe2efc174ff66af5ca5cdf98f5e1cf24a71d7fc74f73fe53758d236c9ebcb6bef1b396d6d37ffe5c18d63b26f33df166c55d3151c4bf69bdb67d6febabd757bcef4ffecfc7a45f9a3f6cf9f3f7976c7ebc299e71700e014af8cd059af1cad084ab3300be2b0eabc8eb67e119c92562daccbf9cb76b6cea353bec26cbfe24cf93eaad2cd3b2388da9cdfd9deaec3ddd3119be28452fe7f9cf31341101c5e6a77c966073b5df7739b5741b87df8cf9b83f5cef6c395935e1b8a988869215d04996e5f993ec5cf91549f38e7adee95f60b5e24a53455bfee9bb789bb2f7d5e1d47fdfb396d97c4dc7e73fbacb4bfe495fd3f3ebfce971fcbe9b43fa70f9e3f69c36c2ccf3dbf000027b0041dc04fa0a32700d742a2c5cc4f00b830083ab80245746ae1b750000000a040d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d001000000dc1c04dddfc36f162b7bb2997bd1010000c0edc8824e6dd499f0bbf6ab9badcd478f6d9e1937119d088a9fdaf7a9d89cf49f761361d938f89d9b86cecaefb64f445a7332448d6cdcebee910d562fb4f1699a5bb5addf479c836c91020000bf011da12b7742975dd74b6151ef942ec262bf038e1b747e4c224422307f2282e445913a0ee86a4cda2763361c93189993b1fc3ef1b4c62bdad43d3acb24885b3b0d0000e046f4059d63f05aaeb977912c0207650bb2d16cebdc4564eaa37de43c4b1dc5eb1f7d25edf5d7e42ccf9cbf74fefac8ae8c76f8e671431a1108299f2b3b1e03b46ca749f9d3f6095ef0d9513a337f1c8315fb8cecbb4695dffd5fca48e95ed0c981f9315dd79fda97e644ea4bb693f43be5531473c89f6d9bd2641ed682aedfbfb4f1b11e976c4fb1f792fd000000de4457d03d4224cd8c980c44c3085f7e1271434127afc35a6718c4a0123ae2a08bb32cab57b4d5e1f4dee98a938df748798d708a7d2bae5514765284c8a3b65f290056e995ef99b6afb449437cd52a7dafef99d96766df19a15fdbd99af2da570b525fbf8c7ba77e492fe663ec4bfeece847e8a4ad5bd9f9cb81aa7fd8bfce7cd5769cd90f0000e06dd482ce47163c9578ca44c7685dcf7935dbbd5684c87478e23c5dbe522804e75adca75974b833417058d059f98af2e7f64998e52716da674737231341d7b5cf827de7c8186e7d9757c79bc09a8fcfcaf875059d716f61cb85fe49d932277db4cee57b3851587f6198ce2f00008077d08bd0f91fcd37ce7110b5db4bc781e6b48ea02baf29161cf292c3d54ebe83b653c6ca77d0a19be52716dad71374a357aec2d03e0bf6ddc5e3e1ea6b2374a3f15919bf770abaf0ff300ffdffe5f5f0aafd000000dec9e83774f2b976f8a5839a44cd46741c6840a259ad63164152b4cf3bd74dbcacbc729d3adc05c1d4da2150d47fd157aea9ecc656316d649f997d67842f035b79fe4bc30e41a4edf2903faa91285f6527dd46118cdb170369ab8e083ebc285b7ee52ab8f63ce5377252a7b4cdf767bb7fd67e000080b791059d733e396a939d5c1025e9b319e1510e71155d4ecfe1d95126d71e71c2b96ee7808bd7c265ba089f240ac5d9a6ebbe5cd5dfd406dfd7782d930587082523bd109e2200e275110f2228961dfabcfc71fb2251f0f54496d8deff314015015db1cfc8be2b48f4ec598cdf26b096ea8f023ee735fee8c48bc4984f97dfe44f7f14e1ef53f37dd43fb16d16cb8f4238afd90f0000e04de808dda51087d88de0dd830fe7e4bfdb9907c166a709fe95e4303a0a000000b7e3b282ce61bd16bc3a3a8a566fccfc7626d139000000f8a55c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908babf075b97000000fc32b2a0531ba1f6b6ddd09ba7e67b0eedb31637749d088a9fdab6c49f2090fb58edfeefd3dfbb59ecacfc6efb96b62d91cd75dd3db201ef9bfaf06efb00000040858ed08960db1cf1a371cc657a1016a34d6c6de4f40939e1611221faa98d85bd28aacf91bd1093f6cd3616f6f99d5d652cf78fdd79ba67ad020000c071fa82ce31782dd7dcbb48168183b285ded15f4574eaebf3df6711c59b1ffdf5256771e6fca5b8103194f2665c19297d7e9cd3fc68aa2193f2a7ed13bce0b3a374667e3d06c5d158622b558e941baffb71c96d55f61fb55fe5d7fc84a8040000f87574059d3f5cbe13c519888611befc242086824e5ec9b6919c200695501001a204c5ec70752fea4468c57ba43c5378d422a9a2276643e451db6f879853f4caf74cdb57daa4c1892e295bfa5edff3e9c46e31a6623f27eab6cf22ca9420d7af7b15a3f613a10300007803b5a0dba2279578ca8848b29cb2088932fa12d8eeb52244a6e3f762a17ead18c459719f46f21802510b17e95f2140a2b8c99f85a960ea08162b5f51fedc3e897382ae17dd8cf4049db7f942fb9cc80bf7f54523820e0000e09be945e8fc8fe61be73b88daeda523c0725a47d075a3829df2ae23e8d67997a01bbf729d08e688cc0b11735f7e1ed86d44d00100007c33a3dfd0c967fd1babd651af8900938e000b4834ab75fc22488af6c5df7c25f1b2f2caf56d82ce51d47fd157aea9ecc6568ef00aba372621eff6ca3ad8de6ae7a8fdbadec7a7b37f23dc010000603759d0f9c84b8cda64c1104449fabcf4a3fc0574397dc76f45995c7b9ce8d8ea76e2a0881295e9227c9228f462245ef7e5aafea636f8bec66b191dc1aad33c5a780691e3af8bf011c1d2e95fcbbcfc71fb2251f0f54492d8fef921755942eae1455d2edbd95744b7dc37b7df8a7d5c3922a2535a1687000000700a1da1bb142218ba11bc7bf0318854bd8b20d8ec34814d850100007e219715740eebb5e0d5d151b47a63e6b73389ce010000c02fe5ca820e000000001640d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d05d0fb616010000805d6441a73716ee6cbba13797cdf71cda672d6e423b112c3fb56d49b1b9ae71007d7d62c5324bdb8ac8e6c4ee1ed980b753c7e1fa7711c7c8b5453ea7b11fed71777b9acd928d7b9629edb79fb3f967ccca7f77fdaf474e8ef19b797fd9e3676dcc5ddee7fa9cef71cffdee39b09a3fda76e717b679fb13c7ca9fd96f06f64f1c2b7fa39f5ffba6b4e97b7d0ffc617484ae3cb2494e0d28276c991e26d77e071f8f9dfa9844a07e6a63612fbade771cd56ce35f5fbf3ca8ced63f2f9e6481dc1cfa35daf47eea797e9cd27efb399b7fc6acfc77d7ff263a5f86ea7384eb719675299f4fec8feedb37dfd7f22fae7f06b3f6078e979f197c995c02fb9fb0ff20bff844f73c3e52904304f41f588f61077d41e718bcf6b327f39c2c0227af14e5db90f5309647537d3a87e3267f8ee2b98741a55b477f7d3ddd6293f3970fa888ad9437a31d9a3c50f1badd777180299f2bdb7d5b6dee1d44e9ccfab58d06f5876f8fce16f20d39e5756dd815e1f467dfc6bcf20d578e0853fd171b3e3f5dfb55f9da7ea3f1797dfb642c951dc5aef1ba9f37d956e5fc18cf9f8035b7d3fc4973328d5539b663fb0ddbbf927f86730245fefa5cde5ded6bd3759f7d2446eed37360d6bffaf974ff9767613d7d918ea0a8a91d748d9fef271ca6957f75fd5bc16aff4bca5fb45f17ec7fb8fc51feb63fd5f30bd01574a36f4832d9dc44da1bc5f2e5a7493a9cf06ea25667800a61b2570ed4399ee490fd371bed9cbd83d9da29f57b0714ef91f29a8527f6adb85651d84911be5969fb05e757de57b6b9212e86d2b6de3dfdfaa313cdf9a40de34573436cb5d9268b1f650b6f3f19978efde6e373a67d2ebf13e38d0072e56d9fddd8792152fd3fa6cfda97e8d957ae17cf43e1b8e6f61bb77f9e7fce43d956faabbf14cdca5facdff559c49e3cbbbe2ed78754e76c7c825db7d744f2b3827a7e8dd29729c6a5c370fd71c475a0e8cf1e8cfcbe7f4bebdf0246fe9795bf62bf11d8ff50f9d3fcf17908cf9b7b3eddfdf559d9f0c7a9055dfa761cbea15b0f655cf8adeb39af66bbd78a40990fbe4c6697af7c98a5dec1e2de7980c42925872dfdeb3be4889433aac7e11fbc957c9d854d844dd10e4dcca3db5d63d6ef6804ada3e9738f055b8ced37191fc7a9f6f939d1ce9f6641732222dc57d7356f5fa267df61ff67f69bb57f6afff9f325f71711323dcf66e54feb8f58f709b3fe79640cb634f969c3262057d217e9b551319c77de8ebadd3be9e45f5eff16b0daffb2f217ec3704fb1f2a7f25bfd49bd3dc97a5537682df472f42e77f94df4c9641d46e2fe2007adf6024ad23e8badfd83ae5dd49d0590fb4f53b42b37ec71504dde81bf5a9f6c5f2edb40d1fd59108929fa7ba3ff3f6257af61df67f6abf49fb17ec3fc6952f2256f5b968efacfcd5faadfb3c6be393793c5cfb0611b859fa886e1b13fdc8b0d429ebc8ca3cb158ce2fb6edad7f531622db67ca9fda6f02f63f59be6329ffce670e7e3fa3dfd0c9672d285a477762420d27ac38a7f68111c153b44f5ee92871d408069fbe39f1a1434e48bb267d6aed1028ea3ff8ca35956d899ffa9efafa77bc721dd96f3e3e67da17c4b9bc92b3d20469dff64a3dd4bda77df97ac7befafa437eb42cdf92f37d73fb8ddb3fcf3fc4cf5bf72528e697f6957d9b95bf58ffc0598ffb179e0f2db2fd9746b506ccd29719b4d163ae3d6e2ebaf6ebbf8c6ce67bbc2f444a9dadaaeb6bf923ddf5af57be62b87646ce943fb3df0cec7fae7ca1937f13aa0fffbc7df6fa077f932ce8dc4398a34279111787bb7d362348ab0e47a1cbe93df876142b3cb45bdd32a175fe325d84597a68e4e14ed77db9aabfa90dbeaff15a263f54cee1d5691efd608a538cd7c539cab7c5ba7ff2a0ba76f5be418a6dfc8fd15db9e53df3fabd08f43f648f6985835e200a9c50ae2c86b14e69ef82fd66e373ba7d7111d3e5cba227763adfbeb97d0bfbc4f195ffe7f207f69bb57f2dff186d0311cee13736ae8fc9c6b3f287e9f1ff15e5333aee9f88f767915e8eff2c7dc6eafa643a7a792eebbc8ee6be6c07352f84e5fcb3f5af53bec26cbfe268f9abf6eb81fd0367cbefe68f5f22fdf59dcf06fc117484ee52884336bea1dc0971a8f5032d0feb6831f0112b599c0ef47d14d5bb02576f1f0000c06db9aca073dc5100e8289f7fbda8d345a8b96fac65e4ed3514d1c51ddfaabf8babb70f0000e0d65c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908baeb7166eb12000000f8836441a737168ed4db6ee88d4bf33dcdc6892bc4cd152782e5a7b62d293647f51bacd6e907fbbdb46d89b38ddce3c6a357c7e1fa7711c7286e3192c67eb487deed313723de43b9b16edadc77b3599b9ee742fdfcd5f364969e372cddd2b73487f17cb71bf7ee685f9d3ea9df5a3b02db33ae3754cd146b44bf7d296f1ab7a2be852f462bf9cd8dc755bab4afd8d8fce99ee17a1c0000de858ed0c922b63932593cb705ae4d974570bc63b68d5bf444a8c9aed7a385561cc8c242fc72bce8aa4f69781d62d3a1cd62644e6cfdf3e22988cbf4f91a6d7a3ff53c5f459e87cf87ca178f7f4b36f3cf938c6dbadfd5530bb350c6f6b966969ebe3458f7d5e3e7cfbd55e33b6b5f699747b8bfb653a77e9d573f03d697b69efd67ed933a8bf3a7635b52fe192bf99ba3eae2f32afff7f655edf6c7c3eda81f00e0147d41e7508b554d6fd19d218ba65fcc07650bda116e881351df9cdd62f92c1c42f50dd9a5a7c557da9bbf35e7fce5e22c0e23e5cde805594529ecbe8b004af95cd9f5d150427412da8927ccfab58d06f587e881b3851c3796f2ba360c9d7f8d75f493ea7f7058aefdaafcc2b90dc6e76cfbd6f2f7c77f2d3d70686e4fe6731af7faba1637e1b39ecf2db3743f46529e9b2b751f725afa2cf7a4362fb4afb18bd1e751fd89bacf35a6fd17dae76d23f363d2a71e2bf993a0b3da18ae6d821300e05be90a3a1f5d281d40262e747b172e5fbe5e2c2b67b021af6f6ab110165c7d78b71720ea954df36ddf0b9432c2e04548bc47ca5b751c1a6b311742e451dbcf127e659b1ba2231c39ee7efdae3e790d95f3491b5a3bda88ad36db6471a66ce1ed27e3d2b1df7c7cceb46f9e7f36feb3f444cfbe43060246e89659e51b8dbb304e0ff608fd79347d0be317c5b0672b6ba57dc53de6fa30ae3f7144d0adb42fd926dbe888a09be40f5f2ce21782a63d0f9f377de128e72a00c09ba9059d5eec0be79c898edfba9ef36ab67b6521afd3cd455a165297af7406c1f916f7693a02312fceeeffd2bfc291584ef8a8a0b3f2759cbc3885ae438b7974bb6b7acead112c8ea6cf3d166c31b6df647c1ca7dae718e69f8dffc2fc48f4ec3ba433d6896e9955bed1b80bc374294bf551eed5b66d6ced056d286fa57d72cff6ec4a59d5fd93fab7ebef157469de3eac6772c04afef405a2db1e45fd4a1b00e0adf42274fe754c23dcac6fe507e938d89cd61174d6377e4fa73ced00a57f45db9533c8188b788db9985bf9acf21d3d4167095eeb77843d67720541d71d1fc7af1674b379d349afc54ddd9eda3ea3f4d9fcb16c5dd86fd23e6d176b7d589dbf759f6b743d9985f635cfbafc3c603426152bf9f74494c397dc3df703009c60f41b3af95c3b8472a19d4765ba741c6cc05e0865f12edae7230cdb82de38fcea959a5fa4b52379a5a07314f51f7ce59acab6c44b7d4f7d7df64a728cd8caddabf28a83ab5fb98eec371f9f33ed9be79f8dff2c3dd1b3ef0cb15791cf97bfd9ccdbc7cdf954dfca1f45d436efa75b7d29edd38c9fb4cf89aed5f6d57691cfdbfa30af3fa14598455d4f62d6bed236d21e27285d7aca3f63257f7fbe8a2d5dfee22707a3350e00e0c56441e79c73fe569d17315990b7cfb2a016dfbe8b7bd7d1e5f41ca7d4db2efaae3d6ed1ddeaae16d02a5d84997666e9ba2f57f537b5c1f7355ecb688755a779f4021f9d805c97855f7e605df72f0ac6d2f16d046727751911caa66e61abdf0b16ff870c314ddaa005cc8c28c042b9e2fc639dd2de05fbcdc6e76cfbe6f9fbe33f4f8f7d6dd8ecbbc2a3fea38d6a7e8a6848657b719ad2f4f35751dbbba6f84395fc6541f7c75d73edd27912e5fc5c6c9fb35bba3f3d33d3fae55ad387d2b6e6fa5208a27efbcc3545eacb6d1db392bf5c1f523f13d25ff7cca8f6f9f999d30100de8c8ed05d0a594c6ffeed562208b5a00b82adbc4fe3230007bfd93711a88b71b67d57ef1f0000c08ff1bffd2fff9b9d7001eee8c0f5b7f822c2214ca2736728a2078b5189efe46cfbaede3f0000801fe5fff87ffc1f760200000000dc83fff3fff97fda090000bf01898ca7e8ae09bf7503805fc07ff97ffd173b0100000000eec1fff5fffebfec0400000000b807fffbfff2bfdb0900000000700f9effebd34e00e870666b150000007803fff5bffed7f09f66d3cf76db0dbdb96cbea7da676d8db8e9e844108cb72d8965c42d2c52db467bbcfd16f4e6aab2c169dd67d9ebeed0b82c6dabe2ec2ef7b8f9726cecaf4bd85cf6f85639723286df4cfacbb67fb9316da0bc4f6c9bd2dcb8eeb6ef6afeb5e7af66defec4b1f205bf31b3afe7c8691d93fe377f1c51fd31c42c7dca5fafffecf801c029fedb7ffb6ff94379e48eecca5e2ed8657a1016fb0594ec2eef9ce6875b3c460bfed2c6c2b2806d7b9249fb7ebda09345f7ab3e45e275cc363ef6f5bb717997adfb472b7d0f2fd9fbb02376ebbed5cf93d49d0fbcf747c7edb3f15afec5e7cf60d6fec0f1f21bbbb9cf2fed7f9cbbf973cd2c7dc25fafffecf801c049fefb7fffeff943b3400f1e707b319f9345e064f1906840b318584753d5824e1f83d4880311a9fa689e4f973f38f0107d70ffaf8f6edae5dcfbe5a7f4d9d1645fcfb27eddfe1041aa50fd9705345db7c746d94fca8ec7413563eecab404a3597f1cc395f697e327f7aa7aa4de944f91e6408a0ee9b6e6f6641bf4edabef97327c24cde72ddb284e50c62bf52795bf4b40d78eadc34cbcfaf97cc2215af9579fbf15acf69f295ff216cfdbc93636fd9f95f7029b68fe5afdaf1e3f00d84957d08d2204f2a0ee75720e5f7e7ac0870fbb73fc4db85fc480bba6c491174fd99987f2fd6b82788fa4970240faa3c58bd4b32d42221abcc85075ec8918cdcbaf173ce9d36647df7ed5c7bafd214fb07d71ada2184785afbff8065f0aa440d9e6862856a46df53db3f67f3ab157cc19e9bfb377feece8dabb335f743b66f6f578d11be6a11f6777cf36de5b19e1d5e9d6975dac08ba99b33bf88c658cfcebcfdf0246feb3e537732af641dfb38cd17f7f2d89f424e6ebfa46e97bf883f5bf74fc00603fffe37ffc8ffcc13be4fc4057e224234ed23de8d675bd1864b67bad088fe9f8fcc252397fcb49560b86b4bf10a0459ee0dc739a4123081c5b99b3fe4dca97b61a0e4e2f82e3f647161649ef5857f2758487191d4dc43ccde2ed18b6df8fe9c87e819180963499133e5ae7faf270823b0bb605fb7a3a7d4e78912be5bbb28af9b787491d42632b8deb8bfd8c2dd2c9bffcfc2d60b5ff6cf92f1304abf6ebcc99cc2cbdc71fad1f4107f0c3d4822e2dc0fe47efcda23088daed65b45848da9b04ddc8498f05dd8c49f99dfede49d0590e5bffce71c5fe39adc348d0055b8572fcffe5f57aaa7fc1be9e4e9f13beeff2da78c521f698d4318afcfa1f95bb7e1c1593cbf93bf65a6321727da07c995fc5fc3950c63efbcde6e4da9cd5fce5fa5f317e007082fff93fff67fe500b01f95c3becd251ed5f7032c387dd955b456e425dee5a76ce0fefaceb57ae2341240b4ed13f2f0eb645c847674ebd729d955f898bea95e0acfd1eb1dbc4e6ed38058afa0fbe724d652f89dfc6fe9277bcc0eb72bd73d2c2de95e7c596942965bb7b757d33fb7a2c9b2a7219befcf5b12f98d461cf7d6753671ffd97898d3de37d21525cb76d357fa4fbfcf5ca57ac38ea23e5bb3cbafd325fbaed6f98f75fc656cfbf0f97aed7b7597ae0b8fd7f77fd8e53e30700a7c982ce39a11c75c982c12d12cee9a7cf668466222e2c74393dc7674689a2400a7965b110f111da208b572ad3e753fdd9ea088b5ebafe8f5bc0f26fca1cb2a07dfa3fb448e96ed1ea081b9b71f975ba08b3b428aeb4df8f45bc96c90b6eb445835e78abbe8960aaed2f8e58eca9af29c2b770a9ab8ca0aedadf8bf0785dec234ebf2847c4504eafec2f6dcb62531c4bfa7fa26fdf9e7df41c0bf33294b932476b569f8fdad17a7cdfdafccd7db91f95435dce3feb5ba77c85d97ec599f245c4f7f30e58ea7f353faa6d99e6e9c219fbffe2fa2387c70f00cea3237497420441f3edf0bd7841b74bc0dd1bf9065e2fbae28c47ceda472cc5797cf3d8000000c080cb0a3ac7770aac22fa6544557e0bba9fcd3770116a83e81c0000005c942b0b3a00000000580041070000007073107400000000370741070000007073107400000000370741077b61eb120000808b91059dda08b6b7ad85de3c36df7368f3c8b839e544108cb72d8965c42d4652db467ba8fd168acd79fd06cb75fac17159dab6c4d95dee910d80877594e3b38759ff0e636e760cabc8c9277e33ea2fdb7ed6c6d7e57d3277529a1bd7dd63b09a3fcebd9d5f38e6ed4f1c2b7f663f008053e8089d88a26da17934c2a04c0f8e77bfb37d04a1f63189f02c6d2c2c0bfc2618663bd8ff0abce8aaceb97d21b38d857dfd6e5cd66c5d8ecf126fee9f50cf63cd9ea3de7e824bb4af23e6ebb6d57696e7fe994e4ef147cfed7b5ed7f22fae2f06b3f6078e979f997e19020038405fd03906afd5464e7144168183b205f9b6dc2cd6d6d15fb5a09303dbd337ecc6f9894855d11f97f7298bf323d427657eca3768957fdfc6c6fdf2537a71b48e4b4fed93b6fb6b725669ce5fb65f1fa994d182691a8152f693b2e3313dcd98bb322d4165d6afc770323e65baf4b5ac67de3fb1674a93b1deea4ef64b73269565d9c19cbbd2ef54a7628fe0d075fa488c94518ce17cfcbbed5f69dfa27dfbed5b645190ccc4a77f5e77d8b7c6cabfbabeac60b5ff25e523e800e01d7405dde81bf4c0e98ff0e5a74570b8203ac7d49c1528ceca5d53e2c88b27e5f08353dcee9174bd7086c5582da4e20045c4c5fb45d47927a805d80e87372f5f8bbb98aeece8dbaffa58b73fe409b62fae5514e3a808918578fde0e1fcc91949dbca7be6e3f3e9c46a3167a4ffcedef9b3bf36eadf438d8dd8a71434d2ef62be761c67cf3ec29ef136f1a23acc73df56d7c7d4e695f19fb57fd4be25fb0edab74cc7ae0533c173700dc918f9fdb82ead2f0b18f95f56fe8afd0000f6520bbaf4ed3e4458ac45273a6eeb7aceabd9ee4d11028db9b0c962e9f2158bbdb508c6453d7d1e3bc4e03c739a41e3701d5b99b3fe4dcaef38002d8cc6ed8f547db6f08e67255fc7b198d1d144ccd308ba595be5ff43fb19796a5c5a1181aadab9643f87699f485f30cde7b7a753a7efd70bc6bfdbbe55fbf6dab78785329abe68fc38f6ecbc4027fff2fab280d5fe9795ff8a310000a8e945e8fc8fde9b457310b5db4bc7c1e5b43709ba5144602ce8664ccaeff4f74e82ce7268f995ebb4ad13c19be8f64f0495b395fa9251db6bc97e0ed33e915744e8ccb25f34fe43c1b962df5efbf6302da31fd97ec8ab5e6787d1733862397fc7de6b2c44e6cf94ff8a310000a819fd864e3eebdf48b58e70d189580c174471def5822a75b96b5970852851fdca75e410459014fdf3afbc36f172fe95ebacfc4a301e78e5d6173c1bed38058afa0fbe724d65b7e2773e3ef2f94bfdeecda4d73f7fdd89fc58fe23fe9e4edb4bf75bd2256265d9a1671f41f7cb8b87fa8bc58c81b35e19ff59fb47ed5bb2ef2bc4c4ac0cf3d97673c6b54fff65aaf4b798eff1be1009ad9fbbd5fc91eefad22b5fd1cdab3853fe2bc60000a0260b3ab7c8e4a84b76a8c1e9a7cf6684c672be137439bd85cd8c12458114f2ca622ee223b44116f754a6cfa7fab3d5119c42bafe8f384715f1f1ced2ff903fa56b81b2c2b8fc3a5d844b5af457daefc7225ecb64a7126dd1a01d4bd5371104b5fdbd70ea471a65ec82dd0da133189f70cfc38b8edc36671f718aa99c71ff4a1b89f0fef09f9548d2f5c7fef97b7d1f635b1a4ac7eb23d3296dd7f8dbe59773b83ffe9e61fbc33de3f68decbbd2be31abcfbf29b4645ed5791d6dfda99de5b8ace79fad2f9df21543a1e8385afeaafd00000ea123749742048df90df87db451a7df8d08a2da2105c156dea7f111cb9508060000007c1f9715748eef14584574e8177f6bd6fdacb7b59845e7000000e0a25c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b839083ad80b5b970000005c8c2ce8d446b6bd6d2df4c6aef99e6663cd15e2e69b134130deb6249611b718496d1beda1f65b28368ff51bf8d6e907c76569db126777b94736b81dd6518e0fbc8272e3e0fd829afce7f227d6d6af3e67f3030018e8089d88a2cd49cbe2e7163de5b4cbf4202cf60b28397d424e509844789636160ee2227d9eedf0fe2bf0a26be771543b986d2ceceb77e3b266eb727ce01cdee6d5f3b867be93ff5cfec0e2fad5e56c7e00800e7d41e718bc566bee5d248bc0c92bbbe5a3bf6a41f7e9ca8ddfc0fdd1483a7ff50d5da247cf18050c1beebafffbf331b7fcfb3636ee979fd267477f7d3daba39d72d9416ca5bc192d985494d51e1b653f29db385a6a14a533ebd76338189f95fe35f671ff97b66ce93326f98bf6497ad9cfd43fb1873f164dee8b6dd47df7f6d211ed54c789f2d33d23fcfc56cfc4de6790fce7f20babeb578fb3f90100ba74059d3fbcbd5c003303a73fc2979f16b1e182e61c637316a2384b774d89232f9e94c396f2fd198af11e49d70b76584cd5022e0e589d052aa2ce3b612dc07638dc79f95adcc5746547df7ed5c7bafd214fb07d71ada2e7a84264205e3f7838bf0819c9236d2bef591c9f41ff42bbb7b35de5b56e215827ccf27f3a3159cc59b1bf1beffc59f042cd95e3e6a69f07ee9e341f1abb8a2dd41c3e5bfe1cb1b1cb2be3e6ec286517f54d21ff99fc7efc97d62f9bb3f9010086d4822e471d9cd329c4494616454be4b8eb39af66bbd78af058c2c32f762e5fb1d88af3acefadc48db4bf10a0451e69f7581c3482cbb19539ebdfa4fcce02ae85d1b8fd91aacf16de71ace4b3ca7798d1d144ccd308bad3e323880d37db8ad3dd04e27c7e0df34b5b8a7c099ddfd1b149e0e1e77e9a97c57c7949f9135c1dda7e8fcf9d1164f29fcabfbc7e75389b1f0060482f42e77ff4de08b741d46e2f6e71ed7e4395b43709bad137f2b1a09b3129bfd3df3b093acb21e557aea7c7a7e2f170f7ef8bd01534f9c3f834f7d58cdae4c87d90fb8af17c4df9239644f400f29fcb5fd0799e97399b1f00a066f41b3af9ac7f23d50a85452766315cd05cb97564c3d7a5222e9d577a23c12082a4e89f7fe5b98997f3af5c67e5570ec5a76f2270d67e8f25cc2ada710a9411a563af5c53d9adf83d3f3ee1cb82b29fa4ef707ab3fcd21e79259b3e9b58362f0863d6f6ff55e5f709f653fd93cf4d598f18c96ce7ed3df2f7f99efafbf90bbaebd7d9fc000007c982ce399a1c75c94e3838fdf4d98cd04cc485852ea7e7dccc2851144821af2cce223e421b64714e65fa7caa3f5b1dae3f2232e2f57fc429ab05de3b69ff43fe94ee16e58eb0b119975fa78b304b8bfe4afbfd58c46b99ec14a22d1ab463a9faf669888b28187b914619bb60f72a822a9c1c1f11cfcfc27efbec3fcffff0a26b4b7702ccd92ff4c3b65f33071dbe2f1d67fe8af2fb54f3c76c43aa478f7be20ef947fc74fb03e3f5eb6c7e008083e808dda51087bf7bc13f871575f9cd7c3861523b9420d8cafb346c2a0c000070412e2be81cdf29b08ae8d781a8e35dd0fdacb7d59845e7000000e0a25c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b839083ad80b5b970000005c8c2ce8f4c6c29d6d2df4e6b0f99e431b63c6cd37278260bc6d492c236e3192dab66fa3d67b526c5eeb37f0add30f8ecbd2b625ceee728f9b2f4d1dc666c177c2dab8f98efd0000803f888ed08928da1cd8a31106657a1016fb05d42308b58f4984676963e1202ed26769dfaf17745e7419a734bc88d9c6c2e9c8a291adeb797217ea63deeeda0f0000f883f4059d63f05aeda8b3cb2270f2ca6ef9e8af5ad07dba725384a572d041a4aae896cbfb8c51c0109d71ff97e3b054fe7d1b1bf7cb4fe9c5d1432e3db54fdaeeaf3dcbfa75fbf5914119d5ff79844cd94fca96c3ebeb7b07513ab37e630cbb73a3183fe96b558f13f9c5f836e7a2d64737b97a74ff67e5efa4167800000097a52be8fce1ed9d28ccc0e98ff0e52701301474ce313767218ab376d79438f2e24939f4208ab67b245d0b8b2026b578917a36c125a2eedce1fcb3f2b5b88be9ca8ebefdaa8f75fb439e60fbe25a454f5085c868bc7ef0707e118d9247dad6bba75bbf13abc59c91fe3b7b6ff73c94ed8380d4f32f949bce46759f45c02a5bcccbdfc1e40b070000c0a5a8055d8a6e840858eb94b3b0b2aee7bc9aed5e2bc263397eef4c5dbec2394721d1dc5709ba42801679a4dd63e76efd666f2b73d6bf49f91d81a085d1b8fd91a382ceca6795ef30a3a3899867b7a0f3633ab25fb8a788b035ed101bebf44dfc2e95bf83662c000000ae4c2f42e77ff4de08b741d46e2fe2807b1110ef9cdf23e88a322bc6826ec6a4fc4e7fef24e8cebd729d085e4997c8a0fa1231b4fde3e1d275846e56fe1ef645660100007e9cd16fe8e4b376d8ada33ee1443b022720cebd76a852978ac8745eb98e04910892a27ffe95e7265ecebf729d955f0946e395eba8fd9ea382ce51d47ff0956b2adb12bff53df575192f79655a5ff7f87e39111fcb7cc4dfd3697b842f13cabe621f358786e5ef6138370100002e481674ce39e6a84b160cc1e9a7cf668466222e2c743996e317cc2851144821af3877111fa10d222252993e9feacf5687eb8f88c078fd1f11254a207891e2ffd022a56b01b9c2b8fc3a5d8459128b2bedf76311af65b2f088b668d082b4ea9bfc01486dff28187b914619bb60f72a82ba54ffc38bae9ce6ec23c2490bda9426c2fac37fde84a388eb6791bf1e9f71f9ab483b9ab907000070657484ee5288a0f9e628c928eaf41b11c1540bba20d8cafb343e6249040b0000e05a5c56d039be536015d1af0351c7bba0fbd96ceb3189ce010000c045b9b2a00300000080051074000000003707410700000070731074000000003707410700000070731074b017b62e010000b81859d0e98d853bdb5ae88d5ff33d9d8d81c7c44d68278260bc6d492c236e3192daf61736842d36cff51b2cd7e907c76569db12d99cd8dde3e6cbb1b19f73b8fd0000007f151da11351b43952d975bf74ac657a1016fb05543c36ea6312e159da5838888bf4f94fecf0ef45577d4ac3eb986d2c9c8ec5fa295b73c62a000080415fd03906afd59a7b17c92270f2ca6ef9e8af5ad07dba7253f4aa71fe2252f5d150725e688802860d77ddffe5382c957fdfc6c6fdf253faece8afaf6759bf6ebf88ad9437a3fa6f1f77a651f693b2e570fbfade4194ceac5f8f61313ed217558e941baffb71cd6d55f619b55fe5d7fc84a8040000b81c5d41e70f6fef4461064e7f842f3f0980a1a073c2e0e0e1fcfeecd0788fa46b6110c4a4162f52cf262844d49d3b9c7f56be1677315dd9d1b75ff5b16e7fc8136c5f5cab28c6511122a3f1fac1c3f94574491e695b7dcfa713a3c59c90fe397b6e9f4594c539a5ffaff3387aed1788d001000018d4826e8b7e54e2242322c472aa2204cae84960bbd78af0988edb3bfb4a1c4421d1dc5709ba422014798278ca6906d66ff6b63267fd9b942f6d3504ac1646e3f647aa3e5b9882c8ca6795ef30a3a3899ea0f36336b24fbaef23ded7178d66fb23083a000000835e84ceffe8bd719e83a8dd5e3a0227a7bd49d08da28a63413763527ea7bf771274e357ae73c12cc8bc1231f7e5e7515bb7bfc76a7f04410700006030fa0d9d7cd6bf916a1ded9a1337e9089c802bb78eecf8badcb52cb84294a87ee53a124422488afef9579e9b7839ffca75567e1dd50af64b2270d67ecf5141e728ea3ff8ca35956d895f198fafcfde9886bcdb2be5601bab9dbdf60bbade87ffbde3fbfe40040000e0366441e7232731ea92054370fae9b319a199880b0b5d4edf711b51a22890425e113f223e421bbc588865fa7caa3f5b1dae3f2202e3f57f441ca82891170bfe0f2d52ba16902b8ccbafd3459825b1b8d27e3f16f15a4647c8ea348f16a455df4410d5f68f82b1279264ec82dd2d21f5f0a22ed7edfa2fa25dee9bf76fa5fdae1c11b9296df7f8000000fc527484ee5288c3ef46f0de831575facd7c1891b020d8cafb343e62398cae020000c0b7735941e7f84e815544bf0e441def82ee67bd71f42c3a0700000017e5ca820e000000001640d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d05d0fb606010000805d6441a7367aed6d6ba13787cdf7d41bd32e1137919d08969fda17aed81cd76f605ca71fecf7d2b620b2f9afbb4736d0edd471b8fe3f803f3dc26fcdd23f6d6204f9ef9b5f4e66f19b65bbfc565e6b63eef23eb5f1b6abffb9bbfdabf9d7d6bf9a69fb657d29d2cb4db9e73c82fd52d9f5b646d5c6e17ce104b8183a42571eb9240f6fb96094e941f88c36a1b589c7467d4c22503fb0b1b0c78baef71d2735dbb8d7d7effa2db6de6fdbebf3d6b3586b11ec3eefb221f9ef9d3f519713a9e75ebd9ec9ba94cf17f647e3ed7b06d7f22fae7f06b3f6a7b5237fde49286fcb5f6f3ceed7a4faf391f10180f7d017748ec102d1dcbb48168193c547be8d5a8b6179b4d4a7fb46aca378f3a3b5be9e6eb1cff9cb0552c456ca9b7165a4747114e9badd77f50d5dca76df769b7bbd60b4a37466fdda4683fac3b777670bf50d5bdab027c299ea97b2f337f5c24673fba631d365f9bcd2ef984f538c7171b49bcbdb4408c6c8dc28fabbd3c191ffdef9331d415733fb727156b058f9a58ffedad1be299af6bfa04c8d29e8547fe4f38a9d01e09be80abad1375459383aa264842f3f2d38c3c5475e49b48b6d580cd502220240444c7402fe9b6fe11044206ced94faf5f99f525eb320c5be15d72a7a0b59f8e6adedd70aafd0b7aa9d9ae88c1ae7a6e8d71f4550ce276d183bad062f1ac338f9729c0d53792bf62de68be15847edf97462bb985352beeb8fbe678429082663a921ffbdf36756049d943d123fb1eebd6b5cc6c8bfbefe2d60e5976beacb925eebd609eb56f84255b74f9e77d70759675cd9f26c1eb60f00bc9e5ad0e5c5407e03521c2c9f9087da72ca2254525ecd76af158132175ebf30d5af3d837828eed3741648ed245604872f67e2444c4165e5eb38163bfa1889791ae7a630eb773482cbd1f47946cf19bec8be5d41e7c7bc9d1f7b7e07f4d38282fc3f9b3fd39bc38ae173e1eab5d7b8453af997d7bf05969e6bb1df51d1285f48dd7814ed937ea93ae5f78ec57801c0cfd28bd0f91fe5378bd2206ab797d16223691d41d7fd46d8294f3b8915c1e1cb9938116da78c95afe3587a826efaca3562d6efb8b5a09b09f605c47e45fd9d36f720ffbdf3677a7338d38f5cfb3fca70751e8d3c2de73fda37cf6ae4fde433256d54f91bc13db533007c2ba3dfd0c9672d285a217162c1182e68ae5cf3956bf98d56feaa4d7e73959cc02b5e09d68b98456b874051ffc157aea96c4b9cd5f7d4d75ff5cad52a5b58b16fcafb901f7d1b11085d86777e4ab88bc3d03fcade8d1b3bfda36d29af186fcf2346920dbbdc22ff80bfdeffc44c68b87adab5c73d2b529fcad7ac17f13ebbfdabf923661b84817d129dbcf26cd57fd4d07e21ec972ff9b7b7320f677f779fca1ffa53f56f646700f85eb2a0738b608e0a6541134449faec1ff0744f73ef3aba9cbe78a8bead7bc2a2b9d5edc441f15ab84c176196162d597cd2755faeea6f6a83ef6bbc96c90b9a083123bd581883c0f4d745488960a9fb278bb1124135214a21751911caa66e61abdf8b25c9abdbd011852d76f9e518f4edeb8902dba7c7fefbfbb49313671bf3b7ed1327528eaf38ae9ead2c4424a6fcf6dc4afddceca6b97efe317fb9ffabeb9329b4e4b9acf33ada35a8d3fee5fcb3f56f6c1fa12f14abe7d3fc8ddba0fcf89a75cb5f3f7b6d7a911f007e161da1bb1422b86ebe60d47f25e6af79c156dea7f111b5eeb7f7314d040d000000fe069715748e3b0a141de56bbe214fa2736728a28b4654020000007e3157167400000000b000820e000000e0e620e8000000006e0e820e000000e0e620e8000000006e0e82ee7a9cd9ba04000000fe2059d0e98d8523f5b61b7a73de7ccfcecd3f037173cb8960f9a96d4b8acd6dfd99b675fac17e2f6d5be26c23f7c806bc751dc666c81687dbf7db59b4df1964635c39bc5ce60d63000000df868ed08960db9cd0a31106657a103ea34d726d1e41a8c9d1502341f7531b0b7bd1559fd2f03a661b0bfbfa5dbffbbbc1b7e300fb789bfd6a11ee3eef7f3e0000000ed017748ec16bbfa34e318bc0c92bc5ded15fe5d1509fff3e8b285eff682a69afbff6ac8e9eca6507b195f266f426bdd3088f44d7523e57763cc6a8b169274a67d66fd8a86bfb41fb56fa5f1cdde5efddbb09f27bc7a7c9effe2f6574d355f99aaefd4ef65ffa5e449427731c0000e06574059d3f5cbe13251a889211befce4e086ce4e5ec9d6ce3c89412d8ee4becd8936af688dc3e3f5f9a1525ee3d863df8a6b153d4110228fda7e4e1434f7956d6e88519e461c28ba8224d24b9ff5ffd389a9624cc57e4ed4e4cf13de3d3ea15fdbf992f25a5a0bee59f9899e7d5ed1ffb2fef95c0200007809b5a04bd189f0dbb1d6e98528542bb68250d9a21b1bdbbd5604ca1426e2085dbed21107e75cdca7e90844ed64a57f85408de2297f168e0a3a2b9f55bec38e3e46de2ce8bafdf7366fc767744878c9778c8fd4b1b54d5e8de7c3fd17ca4f74c7afe87762b5ff083a0000f8417a113affa3fc46b80da2767be938e09cd6117475b425b3e0d0e782c1b1e084bb82e0a4a03bfdca35d24b1ff77f22c8a67cd3f8241e0f77bf8ad02d949fb0ed73b6ff61fc8af677da040000f07246bfa193cf5a50b48ef084131c3a3b57ae1119118759b4cfbf52db9ce8ca2bbda960b08459852d08aafa0fbe724d65377d31eeb1d2845efaacff227ee495664edfc9bbc7277c9950e54bba9a43b3f2133dfb9cedbfcc9d6765cfa23f000000ef220b3ae71c7354280b9a204ad267338234113f16ba1ccbb10a7614cbb5c739c9ad6ee7c08bd7c265ba08b3240ac589a7ebbe5cd5dfd406dfd7782d930583083123bd109e41c0f8eb12dd942d2ceafe45c1d88b6485288fd46544289bba8554ff387da5ff623f1121e9bad8570453afad2def1e9fe7bfcfa27cd7b742f4f6cb5f1bbfc7c9fe4b3f2cbb020000bc191da1bb14e2d0bb11bc7bf061448282602befd3b0a930000000ece6b282ce317aed78557494afd9f662129d0300000038c495051d000000002c80a003000000b839083a000000809b83a003000000b839083a000000809b83a083bdb0b50a0000c0c5c8824e6f2cdcd976436ffe9aef39b4796adce47522087e6adb926273597fa66d9dfea64d63cdcd7e5bde56ffd2b62a6eece41ed7d6b7b4e136c439ec6c61a71fe76de30b0000bf171da12b8f44925df34bc752a607e133da24d7e61184dac724c2f3531b0b7b51539fd2f0bdd476fe4e661b1f7bfbb8719136ee1ffbdf4610b7761a0000c037d217748ec16bb5a3a2238bc0c92bbbded15fe5d14c9fcea1ea28defc68a9afe7f3dfaf9c5f1ffb14c44cca9bd10e7b1a4113079ff2b9b2e331507bedd4b5eda0fe95fe85b34d639abfd788c60da274a67de218860d95dd58c8716729cdd55f44589d88dfea77f3a03937b51a3ff77f69cb4a7adad059db25b73797313a1a6cbb5fcaf0c7b6f9bcca8685fd641eef1574ae5c557f337fa7f36b6e9fa3cf070000dc9caea0f387cb77a23003a73fc2979f44dc50d03947599cb119086250393a71b0eaa0fbd9e1ec5ef488838ef748798de38c7d2bae55f40457883c6afbf51cf3985ef9895efaac7f9f4eec156326f671a22e7ff694366d88af5aa5ecfa1edf5f1189f9bad8408fe343a50501a5e757e8d77676aabcd6d5827a98de994fba9db3f9e1f1a2cad5e3caf26d75f78436cbbd9b6db3789acc158ddc3f9abf89d1f88eec332b7fa9ff0000704f6a4197bfdd3b475038874c746cd6f59c57b3dd6b45784ce122ced9e52b1d4d703ec57d9a05872efd2b046a1427f9b37054d059f98af2e7f649f41c7aa2973eec9fb7e95afd76743412cbd4764d3482c151b4c9b5a1881036f5c818eb742da0e6e9221e65cef8689d172a0f3f67fc3c5a981f1e6b4ef4ae2fcc958dc9fc55f4c77fd4ff49f9abfd0700807bd28bd0f91fbd37c26d10b5db4bc7c1e4b48ea0eb4613161cd650f024169cb4e970ad7c3d7130a1efd0c7e9e3fead0b8a9ea01bbd7215c682ced52fd122f525a169afe6f170e965046a961ec63af4d3ffff53cd8985f9e1e98dd9c1b9b23199bf8ad9f87b9afe9f7f3e0000e0c68c7e43279fb5c36e1dcdba4868e838988038ff36722482a2689f77de9bf898bd526a04c42b059da3a8ff875eb98efa27ce5b5ed9e57413b17ddfc9a7ba2df1367ce5eaedea447a4c7bc4dfd3e9f6862f0b5b7bfd970a354766e9d2dfa7fc8650fa2c69fefe98e698cd0f4f65b30db95747c41ede9e5dc169309bbff9fa607e8dfa7ff6f90000801b93059d730e39ea929d541025e9b319a1d9e1d012ba1cdb798af3b1a244ae3de24473ddce4115af85cb74116649148a934cd77db9aabfa90dbeaff15a263b4ce7fcea348f169ec181faebe2fce587f59dfeb5ccca1fa7aff42f8b90785dec2782a070e851d0f69cbc8c9dff638026821a0583ff438154be1640651b45f87df8cf9bc810f1f72cda57e69fa5fbb6e7f21e8630edcf8f9e7d8b3918055248137115f32c3f03a3f96bd7afe7d7b4ffc3f2dbf4b2ff0000706b7484ee528820d1d1971b2282655dd05d8320d8ec34c147dc3ad1d5260204000000dfc365059de38e024147f9cc6d41aecc243a37a2886e2e47ac000000e0255c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b839083ad8cb68eb12000000f801b2a0d31b0b47ea6d37f4c6b0f99e43fbacc54d542782e0a7b62d2936dff51bc8d6e947fb3dc1dc0cb8e56df52f6d5b229bebba7b5c5b9b362cb6ff2c6feb3f0000c05dd111baf2c8a147e338cbf4207c469bd0da3c8250fb9844787e6a63612f6ada5310be93dacedfc96c63616f1f372ed2c6de7d3fd97e0000803f495fd03906afd58e3aed2c0227afec7a477f954757c979a03a8ad73fda48daebafc9599f39ff76ac92206226e5cde84d72a71128753494942d87a777efedd3b5eda0fe95fe954757c9bd46346e10a533ed638c617f6ecc8e9e9ad8ef6cff0100007e2b5d41e70f97ef4461064e7f842f3f0980a1a09357b2ad330e625039721128c559a0d52bdaeaf071eff4c5c9c77ba4bc4678c4be15d72a7a8225441eb5fd5ae1b142affc442f7dd6bf4f27768a3113fb3851973f7b4a9b36385125654ad9bd7b86f6198ccfaafd8ef61f0000e0d7520bba140109bf1db39ca138612bf2214220e5d56cf75a111ed3e18aa86a0e7f0fcebfb84fd311885a7848ff0a811ac549fe2c1c157456bea2fcb97d123dc192e8a50ffbe76dba56bf1d1d8d1c1574b3f199da6fe350ff0100007e33bd089dffd17b23dc0651bbbd741c7c4eeb08baf29a622618dcff971cbe252c2aba82655190cce80996442f7ddcbf892056f404dda957aeb3f1d961bf63fd070000f8c58c7e43279fb5c36e1de9ba4868e838f88044b3dac891088aa27df13761c989afbc727d9ba07314f5ffd02bd751ff443c7d7df66c9e10db577654a4ba1b5b1bf7d4d767e3b36abfa3fd070000f8b56441e79c5f8eba6441139c6afa3cfda3814574393d876b47895c7b9c28d9ea7602a0782d5ca68b304ba2509c7dbaeecb55fd4d6df07d8dd73259748ad031d20be11904a6bf2ed1cdcf3d826256fe387da57f621f1175e9bad84f447511f58c82b6170995b17b7e485b8c086a2ab740dba73f3eb98caefd5ed17f0000805f8a8ed05d0a71c8dd08de3df8e84492ae4c106c769af09d9b0adfd17e0000003fc265059d63f45aefaae8289fb92dc8959944e7be835bdb0f0000e0a7b8b2a00300000080051074000000003707410700000070731074000000003707410700000070731074b097efdcba0400000016c8824e6f2cdcd936426fde9aef39b44f58dc247622087e6adb9262f35d7fa66d9dfea6cd6a1737c37d5bfd4bdb96c8e6bfee1ed7d6a60d3fddfe1bf090cd92fdd62c67f6d85b7b7e0000e00fa1237422d8362723a70a948eb74c0fc267b409ad8d9c3e21273c4c223c3fb5b1b01735f52908df4b6de7ef64b6b1b0b78f1b176963efbe9f6cff597cf4d1b8fe126a11ec3ebfedf9010080bf455fd03906afd58e3aed2c0227afec7a477f9547577dfefb2ca278f3a3bfbe9ecea9e6fca5f3163193f2665c19297d1e816a8faeeadfdba76bdb41fd2bfd0b67a7c6347faf118d1b44e94cfb1863f8a3edefa0dbeeeb566df1632cfd4e9f15790ee6f46dbea536af1e7f2773b788384f9e018bd5e7070000fe185d41e70f47ef4461064e7f842f3f39a1a14392574a6da42438332504c4c12b07dbbca2350ee7f72221de23e535c223f6adb85651d849112227da7ead7059a1577ea2973eebdfa7134bc598897d9c28ca9f3da54d1b9c1092321b71a2f8d9f6f769ea95be54737018a18b7ddfaec9180feeaf686cb630d734befd4bcf0f0000fc396a41b745272af1941191643931110229af66bbd78af0988e5f9c9575f8fbc8f9751c9c76a2d2bf42a0360eda7154d059f98af2e7f64998e52b7ae9c3fe799baed56f474723b1cc7709ba57b4bfcfc3cfdd34af9a2f00fedaa8bc4ac059f367c05941b7fcfc0000c0dfa317a1f33f7a6f9cdb206ab7177166bd088377deb6a02baf293ae55d47d0add3133c895efab87f1341ace809bad3af5c23ef6eff885c87946db47d1a71cb6dda179d13c47e45ff46cfc08c33790100e0f731fa0d9d7cd60ebb75c4279cecd0214934ab7596e2108bf6c5df542527b9f2cab52f182247059da3a8ff875eb98efa27e2f6eb732602c4f66de42a91eab6a25bf53d569af0def6cf0873a2d77e7dddff456af3c52208b94f491bf4d1c4cdad67d59fa2bf7b40d0010080260b3ae73c73d4250b9a204ad267334233113f16ba9c9e53b4a344ae3dce096e753be75bbc162ed3459825512862215df7e5aafea636f8bec66b99ec3445e818e985f00c02d35f97e8cd2ea73f2b7f9cbed23fb18f8888745dec27a2a0102c51d0f622a121ca246d3122a8a9dc826f6eff02beae8e18f291e95cbe6b9b255a7ddb52bff62122b1edd73e569e1f0000f863e808dda510a779f308c487130e7773b841b0d969827fcdf8d7a34322fa1052000070252e2be81ca3d77a574547f9f66cab710926d1b9bf4e19a1bedfdc0400805fcc95051d000000002c80a003000000b839083a000000809b83a003000000b839083a000000809b83a083bdb075090000c0c5c8824e6de49aa8b7ddd09bbfe67b0eedc71537999d08829fdab6a4d8bcd69f695ba7bf6943573506a3f2df56ffd2b6256eece49e37eec5f6b6fe010000fc5674844e04dbe6486557fed2b196e941f88c36a1b589c7467d4c223c3fb5b1b01735f52908df4b6de7ef44c67c38a63132276ddc3ff6e7d97b7e2a0000c09fa02fe81c83d76a4745471681935776bda3bfcaa39f3eff7d1651bcf9d15f5fcfea68a75c761033296fc69591d2e71134895ea57caeec78ccd35e3b756d3ba87fa57fe16cdb98e6ef35a27183289d691f3d86a3f2a5dc78dd8f6bee8b1abf917d557ecd4f884a000080cbd11574fe70f94e1466e0f447f8f29300180a3a7925db46628218548e5e04841204cd2b5ae3707e7d3ea794670a072de20c7a822b441eb5fd0c61b240affc442f7dd6bf4f27f68a3113fb38d1953f7b4a9b3638d125654ad9f53dd3f2bd285382de789d2d8cfa4f840e0000c0a016745bf4a3124f1911499653152150464f02dbbd5684c774dcded91b87bf8f845647206ae121fd2b04441427f9b37054d059f98af2e7f6498c048dd04b1ff6cfdb74ad7e3b3a1ae909bad5f29dc80bf7f545e3a8ff083a000000835e84ceffe8bd719e83a8dd5e3a022ca775045d794dd129ef3a826e9d91a0117ae9e3fe4d04b1a227e8c6af5cd7ca97792562eecbcf23bb8fa3fe23e80000000c46bfa193cffa3752ada35d17090d2341e7a359ade3164151b4cfbf52ddc4c7ca2bd7b7093a4751ff0fbd721df52fbc82edd93c21b69f47cf1a5b3b66e54bdeed957018bb5e3f7afdd7f53e3e5dff1ae10f0000f007c982ce474e62d4250b9a204ad2e7e91f0d2ca2cbe93b6e2b4ae4dae344c356b773ee4594a74c17619644a11713f1ba2f57f537b5c1f7355ecbe808549de6d1c23388147f5d848b088e4eff5a66e58fd357fa27f611d195ae8bfd44541782280ada9e4892b17b7e485b2c21d52f7fdebe15fbba7244a4a6b42c0e010000fe383a427729c4e1772378f7e0631069ba2a41b0d969029b0a0300005c90cb0a3a87f55aefeae8289fb92dc8959944e7000000e0a25c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908baebc1d620000000b08b2ce8d446afbd6d37f4e6b0f99e43fbacc54d642782e5a7b62d2936c7350e90af4fac5866695b10671bb94736d0adeb6836e355698ac3edfb05f8d323fcd63167f6005c9b9f000000974147e8ca239764d7ff521894e941f88c36a1b589c7467d4c22503fb5b1b0175def3b4e6ab671afafdff55b6cddbbaf1e873bf1d6b3586b11ec3ebf6d7e0200005c89bea0730c5efb1d151559044e5e29f68efe2a8f96fafcf75944f1e6477f7d3daba3a372d9416ca5bc1957464a9f47c824ba96f2b9b23fc3fd8d4d3b513ab37ec3465ddb0fdab7d2ff70f66d4cf3f7ae6f32acdbeeeb566df136947ea7cf8a3cc6397d1bcfd4e6d5e3e5646e1411ddc91cb3589d9f00000097a22be8fce1f29d28d140948cf0e52727397498f2caab8de40467abc591dcb739f1e615ad7138bf3eff53ca6b8451ec5b71ada2b093224476b4fd5a6115fa56b55313a34c8d3851f4ea4ff4d267fdff7462af1853b19f1375f9f384a65ee94b35c6c3085decfb764d6c38b8bfa2b1d9c2586a7cfb97e6270000c0c5a805dd163da9c453464492e56445a8a4bc9aed5e2b02650a1371a6cde1ef419c15f7693a0e583b79e95f21501b01e1382ae8ac7c56f90e3bfa1879b3a0ebf6dfdbbc1d9ffa70fc310f3f37d2b83502db5f1b955709b88efd7a9c1574cbf3130000e06af42274fe47f98df31d44edf6d2116039ad23e8ca6b8a4e79771274a75fb9467ae9e3fe4f04f322b90e29db68fb34e296dbb42f3a2788fd8afe8de6d88c3379010000be9bd16fe8e4b31614ad50382102860e53a27dad3317875db42ffee62b39f19557ae7d4113392ae81c45fd075fb9a6b2ade8567d8f9526f4d267fd17f1fbf57956c4049bf7daafaffbbf486d847b10729f9236e8a3891bbb67d59fa2bf7b40d00100c09dc882ce39f71c15ca82268892f4d98c204dc48f852ea7e7b4ed28966b8f73d25bdd4e1c14af85cb74116649148a9849d77db9aabfa90dbeaff15a263b751162467a213c83c0f4d725ba64899228187b91c6106592ba8c086553b790ea1fa7aff45fec2722285d17fb8aa8e946453bf8ba3a62c8477e73f9ae6d9668f56dd3765d474462dbaf7daccc4f0000804ba1237497429cfacd23241f46a42c08b6f23e8d7fcdf8d7a34322fa1052000000eb5c56d03946af1daf8a8ef235db7e4ca2737f9d32027cbfb1070000f831ae2ce8000000006001041d000000c0cd41d001000000dc1c041d000000c0cd41d001000000dc1c041dec85ad550000002e4616746aa3d944bded86de9c36df7368bfb0b809ee4410fcd4b625c5e6bafe4cdb3afd4d1bceaa311895ffb6fa97b65591cd93dd3d7f7eafb838879d2decf4e3bc6d7c0100e0f7a2237422d8364722a706948ea54c0fc267b449ae4d3cd6ea6312e1f9a98d85bda8a94f69f85e6a3b7f2732e6c3318d913969e3feb1ff6d04716ba70100007c23fff93fffe7fca1111283d76a4745471681935776bda3bfcaa3a93e9d43d551bcf9d15f5fcfeae8a95c761033296f463bec69044d1c7ccae7ca8ec750edb553d7b683fa57fa17ceb68d69fe5e231a3788d299f689631836547663e1cf678d69aefe22c2ea44fc56bf9b07cdb9b1d5f8b9ff4b5b56d2d386ceda2eb9bdb98cfefcd0f74b19fed8369f57d9b0b09fcce3bd82ce95abea6fe6ef747ecded73f4f90000809bf3fffbfffcaff9432124fce1f29d28ccc0e98ff0e52711371474ce511a67790631a81c9d385875a240f38ad6389cdf3be8788f94d738ced8b7e25a454f7085c8a3b65fcf318fe9959fe8a5cffaf7e9c45e3166621f27eaf2674f69d386f8aa55caaeeff1fd159198af8b0df4383e545a10507a7e857e6d67c7ca6b5d2da887e99df9a4db399b1f1e2faa5c3dae2cdf56774f68b3dcbbd9368ba7c95cd1c8fda3f99b188defc83eb3f297fa0f0000f7e4bfff97ff6ffee00541fa76ef1c41e11c32d1b159d7735ecd76af15e131858b386797af7434c1f914f769161cbaf4af10a8519ce4cfc2514167e52bca9fdb27d173e8895efab07fdea66bf5dbd1d1482c53db35d1080647d126d7862242d8d42363acd3b5809aa78b789439e3a3755ea83cfc9cf1f368617e78ac39d1bbbe30573626f357d11fff51ff27e5aff61f0000eec9fffcff3ff307ed48fc8fde1be13688daeda5e360725a47d075a3090b0e6b2878120b4eda74b856be9e3898d077e8e3f471ffd605454fd08d5eb90a6341e7ea976891fa92d0b457f378b8f43202354b0f631dfae9ffffa9e6c4c2fcf0f4c6ece05cd998cc5fc56cfc3d4dffcf3f1f000070637a822e7dd60ebb7534eb22a1a1e36002e2fcdbc891088aa27dde796fe263f64aa91110af14748ea2fe1f7ae53aea9f386f796597d34dc4f67d279feab6c4dbf095abb7ab13e931ed117f4fa7db1bbe2c6cedf55f2ad41c99a54b7f9ff21b42e9b3a4f9fb639a63363f3c95cd36e45e1d117b787b7605a7c16cfee6eb83f935eaffd9e70300006e4c1674ce39e4a84b76524194a4cf66846687434be8726ce729cec78a12b9f68813cd753b0755bc162ed345982551284e325df7e5aafea636f8bec66b99ec309df3abd33c5a780607eaaf8bf3971fd677fad7322b7f9cbed2bf2c42e275b19f0882c2a14741db73f23276fe8f019a086a140cfe0f0552f95a00956d14e1f7e13f6f2243c4dfb3685f997f96eedb9ecb7b18c2b43f3f7af62de6601448214dc455ccb3fc0c8ce6af5dbf9e5fd3fe0fcb6fd3cbfe0300c0add111ba4b218244475f6e8808967541770d8260b3d3041f71eb44579b08100000007c0f9715748e3b0a041de533b705b93293e8dc8822bab91cb1020000809770654107000000000b20e8000000006e0e820e000000e0e620e8000000006e0e820e000000e0e620e8602fa3ad4b000000e007c8824e6f2c1ca9b7ddd01bc3e67b0eedb31637519d08829fdab6a4d87cd76f205ba71fedf7047333e096b7d5bfb46d896caeebee716d6ddab0d8feb3bcadff000000774547e8ca23871e8de32cd383f0196d426bf30842ed6312e1f9a98d85bda8694f41f84e6a3b7f27b38d85bd7ddcb8481b7bf7fd64fb010000fe247d41e718bc563beab4b3089cbcb2eb1dfd551e5d25e781ea285eff682369afbf26677de6fcdbb14a8288999437a337c99d46a0d4d15052b61c9edebdb74fd7b683fa57fa571e5d25f71ad1b84194ceb48f3186fdb9313b7a6a62bfb3fd070000f8ad74059d3f5cbe13851938fd11befc240086824e5ec9b6ce388841e5c845a014678156af68abc3c7bdd317271fef91f21ae111fb565cabe809961079d4f66b85c70abdf213bdf459ff3e9dd829c64cece3445dfeec296ddae04495942965f7ee19da67303eabf63bda7f0000805f4b2de8520424fc76cc7286e284adc88708819457b3dd6b45784c872ba2aa39fc3d38ffe23e4d47206ae121fd2b046a1427f9b37054d059f98af2e7f649f4044ba2973eec9fb7e95afd7674347254d0cdc6676abf8d43fd070000f8cdf42274fe47ef8d701b44edf6d271f039ad23e8ca6b8a996070ff5f72f896b0a8e80a96454132a3275812bdf471ff268258d11374a75eb9cec66787fd8ef51f0000e01733fa0d9d7cd60ebb75a4eb22a1a1e3e00312cd6a234722288af6c5df842527bef2caf56d82ce51d4ff43af5c47fd13f1f4f5d9b379426c5fd95191ea6e6c6ddc535f9f8dcfaafd8ef61f0000e0d792059d737e39ea92054d70aae9f3f48f0616d1e5f41cae1d2572ed71a264abdb0980e2b570992ec22c894271f6e9ba2f57f537b5c1f7355ecb64d12942c7482f84671098feba44373ff7088a59f9e3f495fe897d44d4a5eb623f11d545d4330ada5e2454c6eef9216d3122a8a9dc026d9ffef8e432baf67b45ff0100007e293a427729c421772378f7e0a31349ba3241b0d969c2776e2a7c47fb010000fc089715748ed16bbdaba2a37ce6b6205766129dfb0e6e6d3f0000809fe2ca820e000000001640d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d0c15ebe73eb1200000058200b3abdb17067db08bd796bbee7d03e617193d88920f8a96d4b8acd77fd99b675fa9b36ab5ddc0cf76df52f6d5b229bffba7b5c5bdfb547dcdbfa070000f05bd1113a116c9b239553054ac75aa607e133da84d6464e9f90131e26119e9fda58d88b9afa1484efa5b6f37732db58d8dbc78d8bb471ffd89fc747078deb0000007f9abea0730c5eab1d151d59044e5ed9f58efe2a8faefafcf75944f1e6477f7d3d9fff7ee5fca538103193f2665c19297d1e416b8faeeadfdba76bdb41fd2bfd0b67a7c6347faf118d1b44e94cfbe8311c952fe5c6eb7e5c735fd4f88decabf26b7e42540200005c8eaea0f387a377a23003a73fc2979f04c050d0c92bd9361213c4a072f422209420685ed11a87f37b9113ef91f24ce1a0459c414f7085c8a3b69f214c16e8959fe8a5cffaf7e9c45e3166621f27baf2674f69d30627baa44c29bbbe675abe17654ad01bafb38551ff89d001000018d4826e8b7e54e2292322c972aa2204cae84960bbd78af0988edb3b7be3f0f791d0ea08442d3ca47f858088e2247f168e0a3a2b5f51fedc3e8991a0117ae9c3fe799baed56f4747233d41b75abe1379e1bebe681cf51f4107000060d08bd0f91fbd37ce7310b5db4b4780e5b48ea02baf293ae55d47d0ad331234422f7ddcbf892056f404ddf895eb5af932af44cc7df97964f771d47f041d000080c1e83774f259ff46aa75b4eb22a16124e87c34ab75dc22288af6f957aa9bf85879e5fa3641e728eaffa157aea3fe8557b03d9b27c4f6f3e859636bc7ac7cc9bbbd120e63d7eb47afffbadec7a7eb5f23fc010000fe2059d0f9c8498cba6441134449fa3cfda3814574397dc76d45895c7b9c68d8ea76cebd88f294e922cc9228f462225ef7e5aafea636f8bec66b191d81aad33c5a780691e2af8b7011c1d1e95fcbacfc71fa4affc43e22bad275b19f88ea42104541db13493276cf0f698b25a4fae5cfdbb7625f578e88d49496c5210000c01f4747e82e8538fc6e04ef1e7c0c224d572508363b4d6053610000800b725941e7b05eeb5d1d1de533b705b93293e81c0000005c942b0b3a00000000580041070000007073107400000000370741070000007073107400000000370741773dd81a040000007691059ddae8b5b7ed86de1c36df73689fb5b889ec44b0fcd4b625c5e6b8c601f2f58915cb2c6d0be26c23f7c806badfb8875dd8ec7962ef663360e31e380ef6050080a3e8085d79e492ecfa5f3a96323d089fd126b436f1d8a88f4904eaa73616f6a2eb7dc749cd36eef5f5bb7e8badf7dbf61cab02ba9e077f89ef384bf62fdb1700000ed217748ec16bbfa34e278bc0c92bc5ded15fe5d1529fff3e0b11323ffaebeb591d1d95cb4e51aa0a57464a9f475024ba96f2b9b23fc3fd8d4d3b513ab37e6da378766d4ad311d495fefd8713d15b7e370ed5b9ab4968177d30045e77ec07ed5ba31a3ff77fb1554e9f963f9e1fc9bed2767f2c9bbf47d96854be8c5bbcaed927bac7ed4b74ed3b19bfa9fda6e90000705bba82ce1f2edf89120d44c9085f7e12284341e71c6b7586a710c4a0164772dfe6109b089377d095e8512245ca6b1c67ec5b71ada2e7708320d2f60be2a1bcaf6c73437cd52a6dabeff97462adb0b9f4cf898ef479debfc7bf0f55661d2df4a75c88bdd23dde7eed3874fb3f69df8c50ee76b6acbc76d6827a56fe6c7e78bc2877f5b8b9e7fbe9ee49fd5d69ff9908dd52fb1c3dfbcec66f66bf593a0000dc985ad0a56fefe1b7639653714ec8746ae29c525ecd76af1581321d978f86d4af3da5de81f3e908442d8ca47f85408de2297f168e0a3a2b9f55bec38e3e46629e46d0799bb4f6d3f69df6cf955144a0aa7658af5c9b32e335b3ff45bb127b04908cf196575e7de708e1b4fcc9fc4874c664b5fdc705dd62fb1ca67d85c9f80dedb7940e0000b7a517a1f33fca6f9cd7206ab71771a0bd089d77aeb6a02baf293ae5dd49d08d5fb9ce05c1b87f2ebf44839448afef3f25e816dab78bc7c3d5a32348b3f227f323d113748bed3f2be8a6ed7374ed3b19bf82c67e3bd30100e05e8c7e43279ff56fb85a47b3e6044d4682ce3bafd6718ae029da27afac94385a79e5da173c91a382ce51d47ff0956b2adb1257224ee59599bea619f6cff7cb89e458e623fe1e4bdf5fbf72957b5a51dfefffac7d33c29785ad5cffa542cd9159f9b3f9e1e90abab5f6eb711141d47ef1e8b3d43eb96ed97769fcc6f69ba50300c08dc982ce2dee392a94054d1025e9b319419a881f0b5d4ecfb9da512cd71ee774b7ba9d73550eaa4e176196c48838c974dd97abfa9bdae0fb1aaf65b2c3132166a417c23338687f5d8490387cd331f72335629be787d465098587171db96ed77f71c872df4afff43df283ff0fff3908943026624fd7be784ff947112bfdefb76feb431f897e3d8bfcba7e6156fe687ed8ed2fe7d8acfc2882727addbe19fbdba7ed3b1a3f499fd96f6e5f0000b82d3a42772944906431754fc4e1d6822e08b6f23e8d7fa5378c5e02000000545c56d039acd78e574747f99a6d3526d13900000080435c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908baebc1d625000000b08b2ce8f4c6c29d6d37f4c6a6f99ecec6c063e226aa13c1f253db96149bcbfe539dbee0d30ff67b69db12671bb94736b03d64db3987db0f000000d74447e844b06d8efed138fe323d089fd126b93672fa84ec903f8940fdd4c6c25e74ad1fe7b497d9c6c2be7ed76fb1f57edb9ec747078deb0000007061fa82ce3178edd7dcbb48168193578abda3bfcaa399e43c4b1dc59b1ffdf5f5ac8e6eca6507b195f2665c1929dd3a4eab44a26b299f2b5b0e3fafef1d44e9ccfab58de2d99f29ad88a04ab9f1bab75b6eabb2cfa8fd2abfe6274425000000eca42be8fce1f29d28d140948cf0e52781321474f24ab68d140531a8c591dcb70996e615ad7138bf3ebf52ca33858d1671063d311b228fda7e96f02bdbdc105fb54adbea7b3e9d182d6c2efd73a26efb2ca24c0966e375b1d06bbf40840e0000e086d4826e8bce54e2292322c972fa2254cae84e60bbd78a4099c2c28b91fab5671067c57d9a8e40d4c248fa57089c289ef267e1a8a0b3f259e53bece863a427e8bc4d5afb9587e3cb7d1ff1bebe6844d0010000fc327a113affa3fcc6b90fa2767be908b09cd61174dda860a7bc3b09baf12bd789a08dc8b88998fbf2e3648b36041d0000c02f63f41b3af9ac7fc3d50a813591613212743edad70a0b113c45fbe26fca92385a79e5fa3641e728ea3ff8ca3595ddf4c5115e11f76c16f26eaf94836dac76f6da2fe87a1f9fce3e8db006000080cb91059d8fecc4a85016344194a4cfd33f1a584497d317165614cbb5c7899aad6e273e8a2854992ec22c89422f76e2755faeea6f6a83ef6bbc96d111b23acda385671051feba082b114475ffa260ec8924b1cdf343eab284d4c38bba5cb7ebbf8862b96fdebf95f6bb7244e4a6b42c0e010000e0d2e808dda51041d28de0dd830f231216045b799f864d8501000060379715740eebb5e3d5d151be7a63e659740e000000e01057167400000000b000820e000000e0e620e8000000006e0e820e000000e0e620e8000000006ece15051d5b7700000000ec200b3abdb17067db0dbd796dbea7b331f098b8c9ad29d864735e57af6c707ba86c000000803f868ed0954742c9a904a560ab8f8c92530b469be4dac463ad3e3a11b8189993baf6970d000000f007e90b3ac7e0b56773ef2259041a659b478bc57bd286bda5a08cf7c4e3c7d267b9c71fbbe5d3caa3ad000000007e1d5d41e70f97ef44c90e9e78e0cb4f22ae2716e3ab56117ec529119dfb9bfbfcabe350cf43ae3fe2bf2a0f000000c0afa216743932e644d1b338f83e21bf71b3a25ef3c3dfad085c13e5eb093a87fcb18488481fadf382f2117e6fa7ee49f9753e000000805f4d2f42e7ff28a1116e83a8dd5e76be720de922f2c21f4df8ff7f1a513b041d000000fc3546bfa1f3113b2598eaf4f417a9dbe71d745ea1a63acc83f99d587b3ea360f382d31097083a000000f86b6441e784508e8a659126c26afb6c46d00e083a5d4e2dbe24edf921af6fc3eb559de645e03f49e83ddc3d5af4d9af7cf94b59000000f8f5e808dd15605361000000809d5c4dd001000000c04e1074000000003707410700000070731074000000003707410700000070731074000000003707410700000070732c4127a735d41bf2a6cd80d346c0fe1489b481ef37ed19f7787cfcfbf12927441867c07a1e213db6ebeb599df33a454ebe887d92b36c9b3ace953f6fbfd835a44bfdc74fbc889b2c37e332ebdf18bfc974ec7ba26ee3a8fd2bf94ff1211b4fa772ad39f9f8f7f3b9cddb2f3975246e8cbdcde76db3eadcde6cc77efe25fcc6d8b14c8f7526f20837ff8afa3fdd7cd2cf6a9b9ee6e7caf36b8d8f4e3737162fe658b04f18ff50bf7c6e4e7ce9302fff2cad7d4afbbd065d877fce8cb5f4a5f3fe2dc435e4d0c6f1e3fe4307b5b9ffabe74778b6d69fc55fc91bed7b19b4a07b3887f8f9f1f08bbd1ccc9f3ea77479508bf35dc5411d78e04fd339de4bdaad1db92cd67b062e9c531bef7f84e3c7f46274b6fc4cef78b2fabafbbc7f310cedfe14715339c359ff66f84d9fd5e7608fb2bda3f64ff39f44e667a8efe1846b2b96fcbc56f5c9fc2ee6afd17e6dc369fe192737cc96fe7dba71cbd7fc186ea7a178a1e0cacf22ceb5571cf226ea9cbd27cf6f3d46569b7be316ec5fb6ef297371a71379f5bc48ccecf712bc4d8d536e6e897c01dcb9beffaafeff0cef9affde2ffc65411779977d2f411da19328928fe2b807f9a31a7cbf204a04262df08d43700ba4bb27ab602923e70dd7b23a76ce327ddefd2db076bc1d0e0bae8877e083c5de2e5f8e24937eb58222d3697f7038ea9ae14c67e507a7eafe6fe62d99f56f46edfcd7dabfd18807cf82fd7a88c08a73ca8ad0850779133c16d97ede99956d58c93f64614cbaccf24abaf11cc97397c678fefc6e63325af4ccb44efd4718d57d787e2cd9bebf7e499bfc3589cac6f47f9af91faf6bb44dd49a67f74f45d0a56c3756e9de54ff36966d39fa5a7e93a0db98d676b92ef7d56f188a74790ee4f3fa98cefa3f6d5fd7feae1df1ded03e9747d6d026ff0c57af2a5fda567ee13837fe4d7ef7fff299e897afe9ceffd9f84d485ff48b3996fa2ecf872f77b347eaf3ba7f7efc69fb5e8246d089619d212c359f1c76fab75ec49b3cde409b511a43ca4379c4c10d059d1bd43828879da710fbd60ee8acfc170b3a65df40bf7c6f5fedac47fdeff66f11a3fcb5f6abb4a3f61bf0783cdc62ef1612d796f0ea4f470b5c9a5cf7e5c70756b7d72373561c47d517cf4afe01d2e798d7a317d419c3393f58a454be343e799c8cf1d1af5d7bf599754ddab7876e5f3c07e7c742fb56d62f3d667e2ed4658ee67ca4d7bfe070e3f5183dd4f749bee20b98d527774d9cb23c5b7e6eba3ea439fae99c65f1bc4bffdc1c0e9fc3bcdfe66398ebebce3c32ebffa87d23fbfb6727a4cbcf3afcf8bbff4b1b57bf94867b95bda47c2560ce8e7f18d7ed0bdf43faaa6c312b3fd19d1fc3f19be39f6da92fb5c1d7af9ea3663ec91c54e913feba7d2f412de83e3ea3aa95ce54510e31b037587c681ffae195ff1b0e3ae7f19fdd62ec062c19ad1980559a8967200b62352196717da9a3330d67caefb4bfb49543db7701eb1bb2d9be95fe4d9087a25e48f7b4dfca7f962046dc98b807f3d3cddd0f3746d67d897a41c8c875536c9694f965f16aed3f141ec53333c93f99f3bd454ae71b3ebf91c311baa27d555f166ca919d57d9889fdcab1d8d073ba99b3569983399f30fb67e5abca5faabfd74f295f8f496630bf16fad230cb336adfc8feba5c57469a538d4dbab839396ad7ac7ef7ffb9fda58ecdb6fee70d693d5c283fd19d1fc5b82506eb4b85e56fcb3e5502ae375626d8f712587f14d1a331be137ff9215b34681e34192ce3fe2556279ab46934c90cfc8ffa5dbb6a556f72a07c4fa7fd22c88a09ddb1e9129dbcbbfad7c5fee6b6defe7ddffcf622732c3c8cd502d120c2c368871b9f3511d2c9bfcc6411d4cce65a275d8fc9f0f98dac8c4b7741346c662da833ccf2cfb262bf49fbf3da95d2ade778568fa36bbf3a5f55fe52fd9db5653ad70ef6a56196a7d7be99fd75b9ae8ca382aebbeecdea77ff5fb27fc27d99f491c4d4e685f213e6fcd8b35674980b3a47eed3de351afb5e82a3822e18c0394d6584b59067b876383a2774065acadc42be0fd75ed73e63906d42c4adf8d17b35c1d6ca3ffeca5526a5ae5fecdd2e568baf9c9a093eef5fba6f5a7ee7e1596bbfa397df73f0955ac6cdaf94b7a847aebb3957bc12e8b4c38d4f3bae3bf27790f9a37fdbe7ff6861477eb167316ffcf3b5d958e6a3b4273d6fd61f458c9e5fe1b0a073f8675adb47ae1575aed12b3f707c7eccecd7ac49d5fad53c2fd6732c73a2b269cdd07ea97e7903e0c647dfa7f3f93fc8717668ca1938c1d0ffde7c93be3a9be6fe3ffcfdf5fc9832ebffa07d43fbeb72d5f3d98cc900ff7c687bfaf2b72f3c67c75ff2eb578e12c1d7ebc3acfc446f7e8cc76f4efdca35fc5157fd1c0521e77f6bdb19a71e7fddbe976055d079f1e217523568f26039836df705d190ef7369d6a22b06dde3c812ba0d195d7f7c0d9ad2b4739b220b862e37524cb0a5f2dd24f2e9adc399b6df117e1f12d2ec07aa5f7ec21cab95fe79e6e5370f5e91366bff38ff4afd33fcfcf265e87aa45cf7598d5ff3a35bc346bbf24fa9e74fbbd8ccf01156557f29a01e7e51b2ca5f797ec32beb44b5383accf95b3cc765ff82fded3960312f5f38373f66f6ebad5fdb9c8a73426c97ee8bf62ced17c9ed4fedaed1fd70f7a432646ed54e353a489d2eff0ff7d8e597cf59393ffe710eb058c374f97eec6299d51ad5e348ffebf6d9f65779a5bc687be9771a97b29c1ed5fc1401f0d2f17fb6eb43f10cf5cb5f9b1f8ff1f80d08cf96f457ad714dfb22be6fbade55feae7d2fc39e081d00007c0f67ff4a1fe0104e4c31ef6e0a820e00e01ae828d72db74d80db5246c8db083ddc00041d000000c0cd41d001c05be9fc7e73e3c8ef750000a00041070000007073107400000000370741070000007073ae28e8fce6a6f2bb9bbc87110000000074c9824e6de497a8ff6c5e6ffe97ef39b45f4ddce4cf146c2eedcbd5cb5e38000000006be8089d08b64d44c9aec9a5602bd365df1abd13ff2a8f7ffd111db263b525e862644eeada5f36000000c01fa42fe81c83d79ecdbd8b641168943d3afa276db8590aca78cf57389a267d967bfcb1393e8d2d11000000e097d31574fe70e84e944cc498bc16adaf4ff0e52711d7138bf155ab08bff2a05dfbfee63effea38d4e30f217ec47f551e000000805f452de87264ac7bb0b6fcc6cd8a7acd0fbfb522704d94af27e81cf2c71222227db4ce0bca47f8bd9dba8773e8000000e0cfd18bd0f93f4a6884db206ab7979daf5c43ba88bcf04713feff9f46d40e41070000007f8dd16fe87cc44e09a63a3dfd45eaf679079d57a8a90eff8713f5ab5227d69ecf28d8bce034c425820e000000fe1a59d0392194a36259a489b0da3e9b11b403824e97538b2f497b7ec8ebdbf07a55a77911f84f127a0f778f167df62b5ffe52160000007e3d3a427705d85418000000602757137400000000b013041d000000c0cd41d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d001000000dc9c2ce8f4c6c209bf275c956101d91cd83cad41d5313fcd216e146c6c5cdc2dff9720c79a6de3d09e8671f7fefff6f1030000f87674844e8eddd2e241ce733d7212c48cf608b11e278e16bb2bb2a9b213d2cd291937c16f0c6d5c0700008037321374e9c406499388514a97284b13695b8cc075059d3f785f47a62a4137283fb5ef4bce7a8df7f808a3ba27084495f619ca5b1397c2a38c9eb9b63d8b33671fff7eaaf42f979eea5f695fb269c162ff03fdfead8c9fbe26797d3b751b3fa4bfb17c199f4f759a873f962da56d14d1c569fbcfd90f0000e0cf520bbae44c03e501f9925e3b68cb31cb7db6c31ea58b18710e5a89232f9eb4a089f4caf7ed5765487e7d9f3ff0ff237e7e84736a47edac91f29e29bf200254d9c897afec1504ea678eb6cdda17f248846e1c95ecf57fd63fc9371d3f2fba5cf94ec83fa49dae0ffe5f9fee84a1ea9f08c0a23cc74a846ed8feb3f6030000f88b8c2274c1a16e4e764910387a0e3b61a65b6575c44daffc61fbacb23aedb709e2c24e7348f9c6f9b3223a76d9efa8a05be8df52fd239bb83ab6089d44d05e28e85e653f000080bfc850d055d7561daae9b01566ba555647dcf4ca1fb66f41f08c29a3450daf12249d3e6bccfe2ff46fa9feae4d5cff251aa922944d790e041d0000c00f308dd0fdb34561b4237e7c38072c511ac3a19a0e5b61a78b60da5ea71d7de53a72f8c52bbd43af5cab57983e82b9d967e595e154901c15748e59ff74beeef8f544926fd733bf7295fcbaef09dd8687ff1d5efb071e4bed178ed8cfcd1bffd7d1ae5e5b589e4d070000b82059d039e758fe7e2e5038cc28607c9a88afe28f0a5c9acab7911ce32cbd2a5f7e742f7f1421fff702679c5f9c7dbae69dbeeacfd607557e6cbf252cfa943fdaff470488fe4d5d952ec22cf56da57d22c0d2b54c8e5a2dd86fd6bf03e3a70594eec3d7d3f5cd7f2e4598ff439a945feac8692bed3f67bfb21e5daee66c3a0000c005d111babf8608927d82ee5efcf6fe01000040e4af093a1d05932853f7377137e5b7f70f0000000cfe72840e000000e05780a003000000b839083a000000809b83a003000000b839083a000000809b83a003000000b83996a0db3671ad76eeff8f787a83b12d86def8b5d908d87f96fbb66bec8f06000000f0227a82ce125cfee8abe7471671fea40027d67a473389f8d3f7277ae503000000c00196055de78c51117949c46d824ece110d62aebe3fdd87a0030000007811ab82ae2bc2e4ecce783d08ba0f7f5ee848b021e8000000005ec8ab059dfc3eeef9f9e10f59ef8936041d000000c00b79cf2b57415ebb3efffd50f7261074000000002fe49d7f14e1a377c6efe81074000000002f648fa0dbb76d895c8fdb9454913d041d000000c00bd927e85e03820e000000e085f4045d88b6d51b0b9f858d85010000005e8e25e800000000e04620e8000000006e0e820e000000e0e620e800000000eecc7ffcfb7f03e702469ba99e0b490000000049454e44ae426082, '2026-03-01 15:54:28');
INSERT INTO `kyc_files` (`id`, `kyc_id`, `file_name`, `file_path`, `file_type`, `file_size`, `file_data`, `updated_at`) VALUES
(3, 4, 'configuration_RZ-4.PNG', NULL, 'image/png', 43283, 0x89504e470d0a1a0a0000000d49484452000002740000028408060000008efba7d6000000017352474200aece1ce90000000467414d410000b18f0bfc6105000000097048597300000ec300000ec301c76fa8640000a8a849444154785eedfd4b8eec3cd0a609feebf15c4aac24263569f4a0ab27856e3850e84921518344a3463148e4650bdfe2bea2f1262369bc48728f90229ec183735c146f468af6bac983fc8ffff49ffed3bffff11fff010000000077054107000000707310740000000037074107000000707310740000000037e78a82eef3ebf9efc7e3f3dfafe787990e000000008a2ce83e9efffef3cf3f055fcfcf7f1feae6c7e7577bcfe7632b6c998f7f9f92df146c2eedcbd5ebda73acecf37c3c753fbffe7d7ed4e947fb0d000000f00674844e04db26541e8d7029d383f0a9c5ce9cc7bf9f5f5fff7e7e74227031322775ed2ffb0548fd5fcf42c87e273e3a695c07000000e8d217748ec16bcfe6de45b20834ca1601a9a37f9ee21e11992a7af6f5f9ef53c4e1634bff54e95f2e3d892369afbff67cfefb95f397e2c9acdf9591d27514d3eebb4417533e57f667b87fc94e628f58b6e647442d000000dc8baea07b84489a29287c14ab7c1dbb822f3f09b49e588caf5a45b86d422d10c4a0169cf2ea76bbcf47fe741e4957edf4a2ceff3e2f7c96f21ab115fb565cabe889d91079d4f65b14730a2274000000b09b5ad06dd1a14a3c65442459a243c455195d0a6cf75a11b046f074055d1067c5bd9a8e40d4e548ff0a816afd4eefa8a0b3f21df81d20820e00000076d38bd0f93f4a68c4c5206ab71743808d5fb996d1b606041d000000fc5546bfa193cffa376cad909944cd46740458aaa3797dea68fe48c3bf52dd7e67b6f2caf56d82ce51d47ff895ab16a0ae7dfffcdc1f68000000c04dc882ce899b1c15cb82268892f479fa47038be8726ac12369cf0f27c44c21e3daf354af8545fc14af85cb74116629dae5c569bcee459dea6f6a83ef6bbc96d111c23acda3236a4160faeb12691341b653d0f9c8682a5bcaa8442d00000040838ed05d81dfb4a9f047279207000000f052ae26e8ee8e8ef2d51b3303000000bc05041d000000c0cd41d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d0010000bc96de06f4006f23093abdf16ec1818d837f3b729cd866a3f628b4fa448bbb71f7f65f83b81135cfcff7616c166e71c7f9fdfaf5f97df3f3887d53ffeab5741f8f626d966da3642f502953dae4afc57615f67c7ef4edebd6777f728fde785f70761b6f49d56e8e6fd5c13a0b2f4547e864c215136ce118ac9a5f7f16a9b7c97d8fe3e2acd8ef444e0e41d07d37cd3af64b78c5fa5cf2fdf373b4fe48ffce08ba706eb7b64f38e9289529e9c5f9e4ca7edab622fe529efa38497d36f8086b0ed6d7a4ac730216a0c21674ed832e69fe1bc5b33a9a2add230f47baae28266c3c7b35a5d51befea6f51fed82cb96f970071f9dc4392ca976f51cfe2819c1f0dd6ed9f23b5af40db691a2150fd97b25d1fd3bda9fe6df169cb99dae743fa1bcb97c8e1a73a6d63657ca6ed3f67bf3947c7cfd935d617faefea4d47a8f936ccd263f903fb2df5af98dfb258cb67fd1c55ed77ff3fe7906b4e8ecfe4f91c1136d4766395e6652cbf707e6f7ffe03d25773fe0ee6f7bbedf30ab67ed5f3ea55f3b34fda305ddb2daf87a98cd1fab1b0fe481f9e9feabe3d632fe54f4e17f2624ce655ba4f09baf2be6a5d2cd256049db3ab7974e546778e029ca11674f9613326ba4f97872c4e6899dcf5a41c7d03fb748b4d31c96581718ba2be272c0aae5c09834b3dee1effafbea783b4e7a9cf7695f2c5c9c4fc2b87f7cffad75b0434bd87d5d79fda671cde2ff96a81d59433b48f1386aa7fd6c2b412a11bb6ffacfd069c1a3fef3042fac30be5d08e50e642ba2f736cbf71ffa42d5b9a9425e9fa390a7675e3953ecb584ee6d21ece8ecfd2f339c0cf671139ca067abebdfbf94ff4e66fa297fe6efb9cc5b7eff0fa3c9f9f433a8249ca28e69c6364ff59842e3d97f2b9b6ff106badac486dcd6d7e93a01bf5df13eb2de612c02b588dd0a5f462a21b0f51f781f50e352e4605d5fd0b0fa68ddde6ccc282b4d2bfde22a0311f682b5f55fe52fd23fbb83a8a0882fb7fbd301d1674afb25f9793e3a7edebea95dfc5c8ff739b66e9727d62bf61ffacbeea3a3dd2475dfee6bc4e33b38ffbffb0fd923fb6ab643e5f128da07414f65f29dfb2e34ea4ce5119bdf477dbe72c5bbb0faccf4bf3738cac1d22427cb4cee57b3851a8bf302446f69f09ba6efb672cdc5baf158f4effcf093a3736c3392163f77d7306fe18c3dfd055ac3c70fd0776e2b0137b1ee282507ef75b8f3cbcaf10240b8ba069472b5f55fe52fd5dfbc842e2faa2225c4d798eab0bbac3e3a7edebeadd2fe8e6f61bf6cfeaab35e6394d5eb3bb76f4d2f7727a7c169fcf014341b75abe65c79d489da3327ae9efb6cf5956faf5b2f96910e652b083ffbfbc1e35e6dca89d6f13740b7d699e0569bf91678fa0abdb3cea7b8858577d0478253341a71fc095074e2feae2b0bedcb795e4a4e56190574efafe863d0f71853c883aefc32f3edbc3b9f24a6ad6bf9585a3f75017f5775eb9a6cf8f0f57cf3f65baa7671fdf2e67eb64fbf87bb07af1188d4f62a9fdc211fb0d38357e7a5c5cbdbb05dd82fdc6fd93b6b86725b7efe1e7bb166cd27efd4a595eb9360e51da26911fc351ce383b3e4bcfe780d92bd7773fff09e9e7a88c5efa6becf3705f0cde13b9b3dabdbe3ecfe7e71457de537ea327654ad97e3eb7f78dec3f5a7f66f69f11c647ddefe7ff56a6a46fcf87a4854863be3f725cd0c9d8f7c7bdb54b783ef53def9c3ff00748824e265bfb3a619b583add4f60f7b0a5cf7a927a2715afebdf7304e22292d3dd04778e2b3cd06e72a7eb8ade8365e31c4855be8eb8d4e9e2e0f56298aef7fae75f35c46b99ec78edf6970f665c44e4bad84616b466012ad3e5ffe19eb97d741fc4b1ca9fecfb1faaeb05a83b3e2bed3f67bf3947c74fb55dc623d62df536f3ba932e6d1ed96fa97f7afc5c3effa373f97f5cb4c5f93e8bfe69fb07c499d463b6ced9f1790c9ecf39de59fb1fdaa7fc75ff46e5dbf34fcfef3176fe6dfe8ed35f679f548f7e6ecea3db57b2a3fd93f93945bef4e4b929c243cfd399fd03bdf567cdfe738248dcca4feb4778aeaaf2a40edd77556740b5bd49db487354fa306aab6e43a6b1fd7be60ffc1174840ebe17110c7b162bf8ed3841e61cee5de744132104f833c8b3cbfc871f0641f7bde828df776f7b0017c74740eef9cdbc885eaf467c0000e07520e8000000006e0e820e000000e0e620e8000000006e0e820e000000e0e620e8000000006e0e82ee7af8cd42e52f1e65cf34231de02f30dbd70b00001459d0191b27d6db6a589b5b1e5b70e3e68913c1f253fbfa149b87fa0d38ebf483fd8ea7158cb72a91cd3fdd3d6e3c7a751caeff9b98d9efa730378616be41385bcf8e67718b8f94ff2ab63c8adff8d58fc34cacc91a619d62b29a1f00e08fa12374e53762d915bd140ef5376671dcfb1d4cdc80518eb61a395211983f11a14a474059692f406c3ab4598ccc89addfe1bcf551416fe1cdf63b4bd3ff17474247f6ad9f9f24f0f53db3fc571774c3f9557f49719f7bfd696c25ecc80f00f0e7e80b3ac7c0d9990bee0259044e1ca94453dac55a44a68a7438672867f56d51bcf9d1475f721661ce5f3a9fe9d12c2232e375bbef125d4bf95cd9c5d15df19ee8c42dc163d6af6d34a87fda3fa9375d5714362e8e0692b2ca76a6f649ddfed832b94fd531b75f381f35a4b979d09c8b3919df49fb564882c3160cf3f615f3cbfd3f0bb205fb6e758628ec56ee7a7e7fa0784aafe6ef99f15b793e86e52fb45fc6b688b84b1e730d70f518d1b9f5fc00007f90aea0f387c7772202b2907644c9085f7e5a80878b7158d00b67e20862508b23b96f5be49b57b4de016dedf44e4b9c54bc47ca6b9c7aec5b71ada2b09322441eb5fd82f32cef2bdbdc10a3108df352f4ea5fe9df2882f2e99c7931a6623fe7b4f53d41548671f487b0bb7bb6c3d8e5f3c87e8fe25e1118b5c31f8eef4afb2614277534361cb72fd87d3bbb535e8bd7af4c6711b62c763a369ae7ef8fefd9f19bcd9f95f247ed6fe67467ae043bd763b39e1f00e04f520bbaec70dca25f38d78c5bc4cd455b9c6fcaabd9eeb52238d6c2ed17eae61bbad43b58bc258f2110b51390fe1502358aa7fc59587012a6c3b1f259e53bece86324e6699c97a2e7f056fad775b8dee6edf834874477fa9419d9cfa515119ec20e0be35bb42bd1171016a9ffbd31ecb74f9036ea7457563546334116eaecf77596bf3bbe2f18bf57947f5ed0c93a629781a0030018d08bd0f91fe5378bf3206ab717598c7b113aef3c6c41575e5374cabb93a0335f591a7d32eb8fd767fdeb3bdc89a04a74fa94e9da4f1cb51b0bf525a16cef647c57db3761d8ff61fb2a1e0f97be3f4237b49de3b0a07bc1f8bda2fcb1a0abe6bdf1cc8e6cb4921f00e0cf32fa0d9d7cd682a25d6c4f38d9e1622cceb5750cb2a017ed93573e4a1cadbc72ed3bac4857906cf49c4e51ffc157aea9eca62fc63dd6f559ff74b9fe2f06957016f12baf14f5fd0d0341e0e9d9cf5f7775a5bae3efd5747b67e3bbd4be095dc1b1d0bef06546b54f6c51cde1917dad71abdb33cb3f1adfb3e3f78af247ed171b3fabf28afadc17c65e74ce33cd0f00f087c982ce2dde392a941d721025e9b319419a881f0b5d4ecfb9d8512cd71eb7886f753be751bc162ed3455824e720ce2a5df7e5aafea636f8bec66b99ecb0458819e985030a02c45f17472d0eadee5f143cd9c955842884d4a51ca1675cff4aff042f42e275fd7ba9c0c33bc9944fec2b8225b4c3ae5f8fd1d87e651be507f51ffef3260056c6b7dfbe3965fb74bd8159fb447c3d8bfa6bfbf5edabcb2e09e3b7277f7f7c47f6198fdff9f26339c3f925f5d8f332a489602cafd58cf20300fc697484ee5288435162e08e8820a89d4e106ce57d1a1fb111d177f3be03ec631c95060080099715748e3b2ef03a0ad46cab3189ce010000001ce2ca820e000000001640d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d0fd3dd81a050000e09791059dda48b4b7ed86b539eab1cd3de326a71341f153db96149ba7fed3ee465f9f68f06a66e577dbb7b42d8ab3bddc231bc00efb10c7c8dd6ba7ff1c3f6dffb7503f7f93cda7cfb4ef47fa777716d6c78db5f50d00e0a5e8085db953bbec0a5f2efcf54eee222c469be4dac40d443f2611a29fda58d88ba2fa94860b31699f8cd9704c62644ec6723e7641fcd969f7a47bf4d74590678acd75afc96c7d4cd797d637008057d317748ec16bb9e6de45b2081c942df48efe2aa2534e6c3c8b28defce8afaf677534512e3b88a19437a3058dfa966ef75d0450cae7ca8ec7142ddb6952feb47d82177c76e4c0ccafc7209e9d1ad2649c2a4157a48b2deb7a2afbbbff4b5b74fa68fc52fba4effed8347f8f1aa3817dc286ceaeac942fe6cd7343ec92ae2b8a39361ddf73f36b85a1a09bb52f9e3f1bee71e3579fbbbab77feefff5f8bdabff7a6efab6a9b6e6393e997fd3f933eddf9895f571757d030078395d41e70f97ef447106a261842f3f2d72c305cf2ddcc621dd61b1d40baadcb739c0e615ad77005b3bbdd391053e0b886a811662df8a6b15cdc21e09dfccb5fda273aaee9bd12bdf336d5f699306e728a5ec563888ad36db64f1a5eafa74ceba1873b1af73aae97368f776b6a7bcd6d5f967e3e7f18e3ccc137f50bebb271d989fe8db3f3a79d587f6f0fb765ed50cc757b7e5c8fc9ad08e4b4b7f7e3c0a5b89c0b19edf5efe70bd3f7eefee7fd32e99ab6a8d98cd3fcf60feccfa37a3689fb13efaf4a5f50d00e00dd4822e7d7b0d111a6b319645dc728ae29c535ecd76af152132177c590cadc3e9478b6f6701d50e52fa5747649afa8f0a3a2b5f51fedc3e09b3fcc442fbece86624b6a9110e335bc8ffa7ed9731dad2e4d5f0261027e397b0da51d1b34f23381cf5981f1674d2ff57ccaf09a7049d6b6369ff7d826e387edfd2ff87ab7f7bee8bf15c9a7f8e619da3f93947fab7d5dbae8fcbeb1b00c03be845e8fc8fe61be73788daeda5e320725a47d095d714af723852ce4478980ed1ca77c0a10b66f98985f6f5049de570f22bd7a92d160559e2f170fdd01190c9f825166cd6b3cf6f14744d99f15a5baeb3af443b95c8b0f2a6ebd376d5e3f74dfdcf6548dea2bec5f9b75a67333fe768bbd9eba3a2632f0080b731fa0d9d7cd6bfb1aad3773b79cd70c113e7d42e9622488af6f9573e9b78691cbaf14a68ea700a1163d3da21504614aef9ca3595dd8a1fb1958e583cbcb3d60e4f3ecb2bab2d4f4910fb6a7c2aa73c1b3fcf8243eedbdf953f7de5aa0588ababf9e2302aff05f36bc26141e7e785eb4bea5bfc3d5d9dd7a70dfa371abfefe87f5a539aba1cb3f9e719d439ebdf8cda6ef2b9fb875b083a00f86eb2a0738b5b8eda64271e4449fa6c4678f2bdebe872fa8bafe58c5c7b4464e4badda25fbcf628d345f82467ee17df78dd97abfa9bdae0fb1aaf65f2a2ec1c4d9de6d1822108147f5d848408866587362f7fdcbe48147cbd4898d8deffb183216482834e658bf38b6d52f3c18bbc54b7b3bf38ad548e88a76791ae0562c8df1f3fbbffdb1c58b18f94a7fad0d41f9d78ca5ba4cfcb3f3bbf86e8e7af22d860de3edd0611b61ffe7312462bf6db377e2fedbfa22f941e83f967f76f9b3f2bfd1ba0c7473d0f7a7d4cacac6f00002f4747e82e852ca0e6a27e1fc4a17ef7821e049b9d26f888d52f8d1e58511d0000803fc165059de38e0e5a47d1da6d3ddecc243af79b29a29755c4040000e0d773654107000000000b20e8000000006e0e820e000000e0e620e8000000006e0e820e000000e0e620e8fe1ebf79eb120000803f491674c6c6a6f5b61b7af3d07ccfa17dd6e226a01341f153db96149b97fa0d76ebf4f76e183a2bbfdbbea56d4b9cede51e37debd3adeddbf35e21ce96e41324bbf1e723286df6cfacbb6afb57174799f8c5d4a73e3be7b8c56f347dbee14fcd3f6cbfc2cd2b74d8dd77804fba5b2abf5696ebf1913fbccda7fba7fd47faafe597e80df8e8ed08960db1640b778568ebd4c0fc262b489ad8decae2e3bfabb8773e4307e6a63615934be8c5314aec2a47db38d857d7e675719cbfd63f7ddc8023d126cb3f48bd211d33e72aa3ed7cf9b3c37f9e82a7fb4dcbe315ccbbff87c1accda9fe65efebc9350de96bfdeb87b5aff84a97d66ed3fd93fea3f57ff343fc06fa72fe81c830768ef6299c82270fa705ac24444a63eba47ceabd4513cf710ab74eb68a2af6775f4532e3b88a19437a305838a62da7d57df10a56c39fcbb7bafc1a4fc69fb04b1abbb66093e33bf1e8341fd21fae16cad2224d2c75d11d478be68a8c3cd83fa5c4eebe831ddbf497aea9fb43d4772f41817f9dd7dcdc6cfd5fc71ff175baea72fd2117435b540a991397dc66159f9579fcf159af6bfa04ccdec249699fd6634f699b5ffc5fda3fe9df55758f31be057d31574a36f38f2607544c3085f7e7a20870fa773bc46b83e381bb5808b831691114585b4b71018de816fed94faf5f98d525ee31062df8a6b15859d14befee21b622b8c56e895ef99b6afb449431413d2f7de3dfdfe451194f3491ff738cd87ca1b04d836bf64acb6b1917bbd78cf7d9da547bc280df3ccd7e5e640aaf3d389f962cecafc70fd499f43bfb7b369e5b5b42e7f96becc8aa09b39af83cf60c6c8effbb7f47c2e60e5976b510c7b8af15c253c5741504fec73b6fdb57d67ed7f49ff22d4bfbffefade3a3fc06fa71674f961f11110cbe944c76a5dd70f5b66bbd78a10998ecd3fb8f56b45a977e03c258fb1806be122fd2b04aae558e342505cab088e7d215f51fedc3e09b3fcc442fbece86624b6e998a06bf334361de1da5e44c8743b6763b13a56d67d82dc5bd83da1ed2f736c4b9357dba5c398a52fd26ba36268576fc776de2cd3c9bffc7c2eb0342f644c8e8a2ef9c2e4e670af7dbbe665cdaa7d67ed3fda3fea3f57ff6a7e80df462f42e77f34df3c1483a8dd5e460fbba475045df71b57a7bceb08ba75ccf2130bedeb09bae92bd748affe73824e04adcbafbe2414796763b13a565d9b87f9d35eefe004c3435e99f7f2ccd2474ce7453ff22975ca3c3f1a7958ce2fb6ed3d9f535623b73bc7a4a6fb2cacd6dfb2cfbeb3f6efef1ff59fab7f5f7e805fc6e83774f2593bfcd6d1ef7f60334387e1ca35225722488af6c5df442551b0f2caf56d82ce51d47fd157aea96c4b9cd5f7d4d74fbd72f5ed76223de67dc4dfd36de32163e5ca52651f7de5dab39ddcaf7f545f13beac6c79fd971a354767e9cbcc049df96c385bbbf6ebbfdc6be673bc2f4482eb7159cd1fe93e9fbdf2159dbc62bffa8f1ada2f14fdf24bfbcbf8db5f48fa6d1f31b7cfacfde7fa47fde7eadf39bf017e2359d0392793a336d949ba87c439ddf4d98cf00cc5858d2ea7e7d8ec28537868b7bacb884f9d2ec2272d1af270a7ebbe5cd5dfd406dfd7782d93170c114a467ab13089e888d7457cc8b7c54eff5ae6e58fdb17f1c2a91fc914dbfb3f2670e596f7acd42ff6aefad81185167a0c4418fa05598bcf28d0c33db218c736a539364c8fffaf28e7d0c38bba9c2e0ec2d92fd941c4e9b3482ffb374b9fb1fafc988e48c6b5ceeb689f9164876ddcf6e59f3d9f9df2157d475a3d9fcd1fa50883f2e36bd62dbf1d8939e4c897ec336bff89fe51ffb9fa77cc6f805f8b8ed05d0a115cb558b9192258d605dd6b0882cd4e137c444d16bf03b6f5826e8780010000806fe2b282ce714701a1a368f637d43722426d109d3b43111d34a24a000000f0835c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b839083a0080d723fbe17df7b64500f0872904ddc7b639a3de917b1539b9c16fa6fb25f9db85ccda18b7bcefa3dc3876f762b89a3f6e6eb9732fb669fb9bcd2d8dcd51873c82fd52d99d6d4fe49405b1b1afe34ddb94bc056333e73b227bfded6d7fda54f9cc46a7e6c6d2c237ecd798dadfb0b885cd2bfaffd3ccd6b7125963eacdbbc50e21bfac4f777e0600e082684127bbe88705f7e184d15e31a2e81c6d541f13557f83957de7f2d13efee8ac7d3bbeafe50fd73f4514ed1674e3f67b4177c2b986f2b6fcd6c6c4e2109eee9eedf8adfbd1d8ed1751cf118df4fbaca069ca3f39e76a66ed6fe67b25e8deddff77336a7fa6b3be69cc395ee7739faf6e0f00b811af8cd06516163c61b6789e750056fe2c5a5fe008dfed5c1b41e7ca7f9e2abf3a3ac739e3d4fee6b8273786e9738ec214476fb9fbaa08622a43f2e748a331c6a6b313e2f9aea17c374e6a0e86e89413e22a822965971b4fd74703b97a16234829faa5db956d92ca503669da2f631fd3347afef9f9f8a9ee5b110f1569ced982a16fbfc0c03e8bed0f75ca3ca8ecfa8afe9f985f5276c853ce8fe5f217da9f99ae6f76744ed69e62be4a9d2f5c2f00e08f53ff86eef1708ba53806bfd8cba2d62e4c535604dd6c319374e73476d79d30f27b8794ea3cbb985af9e59a7608e2507647d2248218f237e58b5d9d13ca4e6967f93e32593894e098938d1a91e0ebdbdaf0e99c65311e92dfb5277f16bce80976f65144774f1d4d6ceac93c8a7bc5816b872a76f14e38df23b6da9c7628773bdf530ecf5f3ed5a2331f1a27ece8b77f135c569ae4f3afe1637952f6f439a9d0af5ddbbc63fbadd867defe34f76cbb9ee9ffd9f9e5cb57cfc491f247edcfc8733118b760e736dd1474abf3130060861674290a22df723fddc2ffe1c45d71f32a93054f9045cffcf62bb885eed42bdf4efe1c7151ccdad963d8fe4447242c110f22d7ed0b0e51b7d9ddf3b9287a97044b78d59eca2b04a0e4af6c17a8ecbc38f6e63d7edcb6b2e50b4529e86c71b5dd230255e7df9cfb0ae2cca5effe39f04257ecd1dab7db7ec74cd0147366c15635a97cb30d13fbadd867d6fe50a794734cd075fbff82f9f58af247edcf0cc7cdd9a66e73044107006fa58ed009b23086c56e41b4584c1d551959d1f81f0dcbb76f236d85e5fcb2981e155b83f697f41ddf12d582efc7a52aaf71123d3afdadf367a7286358dcbfd897e9d8873ada7bc411bab6a4df40c6fbf4fc9b0b3a8513c432179623748e608bd04fff7f793d68d8cc6e7fe0b0a059a45ffedc7e051dfbccda3f6beff1fe9f9f5faf287fe9b99eb4a19756474ccfad41000015ada013c71017b5a30bcecc5199e5868894fecb54db213d5cfbda6fd6ebf923ddbef5ca5774f28ae0a8ffa8a1fd0bc47ef9923fff5187bbcf47148bfc2224c529c47ba2f85815bf8d2032f3876b9678129133fd6de56cec1da6d3139b3a679a5f9fc5df83e9f19bbd722ded2765b8b69863dcc1ddff94df6049db24af2fafbd6fe4b4b5ddfc970b37cec9becd7c5cb0554d57702cd96f6e9f59fbebf6d6ed39d3ffb3f3eb15e58fda9fe9b6419eedf1baa1d727698f35bf00000ef1ca089df54ad38a90340baf200ea9ceeb68eb17c12969d5c2b99cbf6c67bb3077ca5798edf7045199cb16f1d1dc33283fbe66ddf21b91462fc242fafedfe855e5bbb1b1fae8c7df14420fef8452fe7f9cf3dbda98fa55b2d9c94ed776d8e65db09d17c412758a7df4cef6a3df7f1117f2dbcf5cf65efbf83994ea13e7bcd5bdd27ec18ba494a6ead77df33671f7a5cf3d8152a37f3fa7ed9298db6f6e9f95f697bcb2ffc7e7d7f9f263399df60bb3f54dda301b4b1f158d7957c71d0060094bd0015c111d3d01b816122d667e02c00f82a0833b5044a78ca82f0000c09f064107000000707310740000000037074107000000707310740000000037074107000000707310747f0fbf19acecb966ee3507000000b7230b3ab51167a2de18d7da5cf4d8e6987193d089a0f8a97d9d8acd47ff693711960d46dfb929e8acfc6efb44a47dcd4e8e908d79dd3db281ea8536364d73abb6f5fb8873902d500000e037a02374e54ee7b2ab7a292cea9dd04558ec77c07103ce8f49844804e64f4490bc28328efbb90a93f6c9980dc72446e6642cbf4f3cadf18a36758fc63209e2d64e030000b8117d41e718bc966bee5d248bc041d94238b3b4be2e22531fdd23e755ea285eff682b69afbf266775e6fca5f39f1ded333fae490442cae7ca8ec7fc2cdb6952feb47d82177c7694cecc1fc760c53e23fbae51e577ff973252ba177472207e4cd7f5a7f6a53991fa92ed24fd4ef914c51cd2c7a6f9c8662de8fafd4b1b1beb71c9f6147b2fd90f0000e04d74059d9c2beac492193119888611befc24e286824e5e87b5ce3088412574c44117675556af68abc3e7bdd315271bef91f21ae114fb565cab28eca40891476dbf5200acd22bdf336d5f699386f8aa55fa5edf33b3cfccbe3342bfb6b333e5b5af16a4be7e19f74efd925eccc7d897fcd9d18fd0495bb7b2f3970355ffb07f9df9aaed38b31f0000c0dba8059d8f2c782af194898ed1ba9ef36ab67bad0891e9f0c479ba7ca55008ceb5b84fb3e8706782e0b0a0b3f215e5cfed9330cb4f2cb4cf8e6e462682ae6b9f05fbce9131dcfa2eaf8e3781351f9f95f1eb0a3ae3dec2960bfd93b2654efa689dcbf770a2b0fec2309d5f000000efa017a1f33f9a6f9ce3206ab7978e03cd691d41575e532c38e42587ab9d7c076da78c95efa04337cb4f2cb4af27e846af5c85a17d16ecbb8bc7c3d5d746e846e3b3327eef1474e1ff611efaffcbebe155fb010000bc93d16fe8e473edf04b0735899a8de838d08044b35ac72c82a4689f77ae9b785979e53a75b80b82a9b543a0a8ffa2af5c53d98dad62dac83e33fbce085f06b6f2fc97861d8248dbe5217f542351beca4eba8d2218b72f06d2561d117c7851b6fcca5570ed79ca6fe4a44e699befcf76ffacfd0000006f230b3ae77c72d4263bb9204ad26733c2a31ce22aba9c9ec3b3a34cae3de28473ddce0117af85cb74113e49148ab34dd77db9aabfa90dbeaff15a260b0e114a467a213c4500c4eb221e44502c3bf479f9e3f645a2e0eb892cb1bdff63802a02ba629f917d5790e8d9b318bf4d602dd51f057cce6bfcd1891789319f2ebfc99ffe28c2dfa7e6fba87f62db2c961f85705eb31f0000c09bd011ba4b210eb11bc1bb071fcec97fb7330f82cd4e13fc2bc96174140000006ec765059dc37a2d78757414adde98f9ed4ca273000000f04bb9b2a003000000800510740000000037074107000000707310740000000037074107000000707310747f0fb62e010000f8656441a73642ed6dbba1374fcdf71cda672d6ee83a11143fb56d893f4120f7b1dafddfa7bf77b3d859f9ddf62d6d5b229bebba7b6403de37f5e1ddf6010000800a1da113c1b639e247e398cbf4202c469bd8dac8e91372c2c32442f4531b0b7b51549f237b2126ed9b6d2cecf33bbbca58ee1fbbf374cf5a05000080e3f4059d63f05aaeb977912c0207650bbda3bf8ae8d4d7e7bfcf228a373ffaeb4bcee2ccf94b71216228e5cdb83252fafc38a7f9d1544326e54fdb2778c16747e9ccfc7a0c8aa3b1c456aa1c29375ef7e392dbaaec3f6abfcaaff90951090000f0ebe80a3a7fb87c278a33100d237cf949400c059dbc926d2339410c2aa1200244098ad9e1ea5ed489d08af74879a6f0a84552454fcc86c8a3b6df0e31a7e895ef99b6afb44983135d52b6f4bdbee7d389dd624cc57e4ed46d9f45942941ae5ff72a46ed2742070000f0066a41b7454f2af1941191643965111265f425b0dd6b45884cc7efc542fd5a3188b3e23e8de43104a2162ed2bf42804471933f0b53c1d4112c56bea2fcb97d12e7045d2fba19e9093a6ff385f6399117eeeb8b46041d0000c037d38bd0f91fcd37ce7710b5db4b4780e5b48ea0eb46053be55d47d0adf32e41377ee53a11cc11991722e6befc3cb0db88a0030000f86646bfa193cffa3756ada35e1301261d0116906856ebf8459014ed8bbff94ae265e595ebdb049da3a8ffa2af5c53d98dad1ce115746f4c42deed9575b0bdd5ce51fb75bd8f4f67ff46b8030000c06eb2a0f3919718b5c982218892f479e947f90be872fa8edf8a32b9f638d1b1d5edc44111252ad345f82451e8c548bceecb55fd4d6df07d8dd7323a8255a779b4f00c22c75f17e12382a5d3bf9679f9e3f645a2e0eb8924b1fdf343eab284d4c38bba5cb6b3af886eb96f6ebf15fbb8724444a7b42c0e010000e0143a4277294430742378f7e06310a97a1741b0d969029b0a030000fc422e2be81cd66bc1aba3a368f5c6cc6f67129d030000805fca95051d000000002c80a003000000b839083a000000809b83a003000000b839083a000000809b83a0bb1e6c2d0230dea01a00002ab2a0d31b0b77b6ddd09bcbe67b0e2db87113da8960f9a96d4b8acd758d03e8eb132b9659da5644362776f7c806bc9d3a0ed7ff4dccecf753981b330bdf209cad67c7e3c6dababf26e5bf8a2d8fe24f07f1e330136bf6e6d7ebf90100fe183a42577e239653034ae1507f6316c7bddfc1c463a73e2611a89fda58d88baef71d4735dbf8d7d7effa2db67e87f37efb59aa6fb6df599afebf38123ab26ffdfc2481afef99e5bfbaa01bceaffa4b8afbdceb4f632b61477e00803f475fd03906cece5c7017c82270e2487b477f9547537dfefb2ca278f3a3bfbe9ece29e4fca5f311b195f266b4c3159119afdb7d97e85acae7cafe0cf7373675655a82c7ac5fdb6850ffb47f526fbaae286c1ccfc64d69758436b54feaf6c79ac97daa8eb9fd64bc529a9b07cdb9b193f19db46f8524386cc1306f5f31bfdcffb3205bb0ef566788c26ee5aee77f7eaafbaaf97b66fc569e8f61f90bed97b12d22ee92c75c035c3d46746e3d3f00c01fa42be8fce1f29d88802ca41d5132c2979f16e0e1621c16f4c299388218d4e248eedb16f9e615ad77405b3bbdd3122715ef91f21aa71efb565cab28eca40891476dbfe03ccbfbca3637c42844e3bc14bdfa57fa378aa07c3a675e8ca9d8cf396d7d4f1095611c1f528fbbc7ff9bf38cecf728ee1581513bfce1f8aeb46f4271924763c371fb82ddc3d9b6feb3d8a2eaeb2cc296c54ec746f3fcfdf13d3b7eb3f9b352fea8fdcd9ceecc9560e77a6cd6f30300fc496a41971d8e5bf40be79a718bb8b9688bf34d7935dbbd5604c75ab8fd42dd7c43977a078bb7e43104a27602d2bf42a046f1943f0b0b4ec27438563eab7c871d7d8cc43c8df352f41cde4affba0ed7dbbc1d9ffa70fd5e9f3223fbb9b422c253d861617c8b7625fa02c222f5bf3786fdf609d2469deecaaac66826c8429dfdbecef277c7f705e3f78af2cf0b3a5947ec3210740000037a113affa3fc66711e44edf6228b712f42e79d872de8ca6b8a4e79771274e62b4ba34f66fdf1faac7f7d873b1154894e9f325dfb89a37663a1be2494ed9d8cef6afb260cfb3f6c5fc5e3e1d2f747e886b6731c16742f18bf57943f1674d5bc379ed9918d56f20300fc5946bfa193cf5a50b48bed09273b5c8cc5b9b68e4116f4a27df2ca4789a39557ae7d8715e90a928d9ed329ea3ff8ca3595ddf4c5b8c7ba3eeb9f2ed7ffc5a012ce227ee595a2bebf6120083c3dfbf9ebaeae5477fcbd9a6eef6c7c97da37a12b3816da17becca8f6892daa393cb2af356e757b66f947e37b76fc5e51fea8fd62e367555e519ffbc2d88bce79a6f90100fe3059d0b9c53b4785b2430ea2247d36234813f163a1cbe939173b8ae5dae316f1ad6ee73c8ad7c265ba088be41cc459a5ebbe5cd5dfd406dfd7782d931db6083123bd70404180f8ebe2a8c5a1d5fd8b82273bb98a108590ba9423f48ceb5fe99fe04548bcae7f2f1578782799f2897d45b08476d8f5eb311adbaf6ca3fca0fec37fde04c0caf8f6db37a76c9fae37306b9f88af67517f6dbfbe7d75d92561fcf6e4ef8fefc83ee3f13b5f7e2c6738bfa41e7b5e8634118ce5b59a517e00803f8d8ed05d0a71284a0cdc111104b5d30982adbc4fe3233622fa6ede77807d8ca3d2000030e1b282ce71c7055e47819a6d3526d13900000080435c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908babf075ba3000000fc32b2a0531b89f6b6ddb036473db6b967dce47422287e6adb9262f3d47fdadde8eb130d5ecdacfc6efb96b64571b6977b6403d837f5e1ddf6010000800a1da12b776a975de14bc75cefe42ec262b449ae4ddc40f4631221faa98d85bd28aa4f69b81093f6cd362ef6f99d5d652cf78fdd79ba476f010000c071fa82ce31782dd7dcbb48168183b285ded15fe5d14372dea68ee2cd8ffefa7a564713e5b283184a7933ae8c946e1f87a491e857cae7ca8ec7142ddb6952feb47d82177c7694ceccafc7209e9d9ad28a08ad941baffb71c96d55f61fb55fe5d7fc84a8040000f87574059d3f5cbe13c519888611befc242086824e5ec9b6919c200695501001529cb559bda2350ee7d7e74b4a79a6f0a84552454fcc86c8a3b6df0e31a7e895ef99b6afb44983135d52b6f4bdbee7d389dd624cc57e4ed46d9f45942941ae5ff72a46ed2742070000f0066a41b7454f2af1941191643965111265f425b0dd6b45884cc7efc5827138fd48c8481e43206ae122fd2b04481437f9b330154c1dc162e52bca9fdb27714ed0f5a29b919ea0f3365f689f1379e1bebe6844d00100007c33bd089dffd17ce37c0751bbbd7404584eeb08ba6e54b053de7504dd3aef1274e357ae13c11c91792162eecbcf03bb8d083a0000806f66f41b3af9ac7f63d53aea351160d21160018966b58e5f0449d1bef89baf245e565eb9be4dd0398afa2ffaca3595ddd8ca115e41f7c624e4dd5e5907db5bed1cb55fd7fbf874f66f843b000000ec260b3a1f7989519b2c188228499f977e94bf802ea7eff8ad28936b8f131d5bdd4e1c1451a2325d844f12855e8cc4ebbe5cd5dfd406dfd7782da32358759a470bcf2072fc75113e22583afd6b99973f6e5f240abe9e4812db3f3fa42e4b483dbca8cb653bfb8ae896fbe6f65bb18f2b4744744acbe2100000004ea12374974204433782770f3e0691aa7711049b9d26b0a9300000c02fe4b282ce61bd16bc3a3a8a566fccfc7626d139000000f8a55c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908baebc1d62200e30daa0100a0220b3abdb17067db0dbdb96cbee7d0821b37a19d08969fdab6a4d85cd73880be3eb16299a56d45647362778f6cc0dba9e370fddfc4cc7e3f85b931b3f00dc2d97a763c6eacadfb6b52feabd8f20872b28bdf6cdb8dc37cfeda9b5ffbd345fc3822f600000a7484aefc462ca706940b6ffd8d591cf77e07138f9dfa9844a07e6a63612fbade771cd56ce35f5fbfebb7d8fa1dcefbed67a9bed97e6769faffe248e8c8bef5f39304bebe6796ffea826e697e0dbeac241a5b09753ef7f9ce021700e0a5f4059d63e0eccc0577812c02278eb477f4577934d5e7bfcf228a373ffaebebe99c42ce5f3a1f115b296f463b5c1199f1badd7789aea57caeeccf707f635357a62578ccfab58d06f54ffb27f5a6eb8ac2c6f16cdc9456476853fba46e1f6991fb541d73fbc978a534370f9a736327e33b69df0a4970d88261debe627eb9ff6741b660dfadce1085ddca5dcffffc54f755f3f7ccf8ad3c1fc3f257e657622ae85c3d46744ee64611b1973a5f28c601006e4d57d0f9c3e53b110159483ba264842f3f2dc0c3c5382ce88533710431a81c813818e778d322dfbca2f50e686ba7775ae2a4e23d525ee35862df8a6b15859d1421f2a8ed179c67795fd9e686e8ec1ae7a5e8d5bfd2bf5104e5d339f3624cc57ece69eb7b82a80ce3f8907adc3dfedf9c6764bf4771af080c3dbfa6e3bbd2be09c5491e8d0dc7ed0b760f67dbfacf628baaafb3085b163b1d1bcdf3f7c7f7ecf8cde6cf4af9af88d0053bb7e9cd33b1f0ac0200fc196a41971d8e5bf40be79a718bb8b9688bf34d7935dbbd5604c75cd865a16ebea14bbd83c5bb2310b51390fe1502d5722c47059d95afe3b8ece86324e6699c97a2e7f056fad775b8dee6edf8d487ebcf9cf1d07e2ead88f014765818dfa25d890501a148fdef8d61bf7d82b451a7bbb2aa319a09b25067bfafb3fcddf17dc1f8bda2fcf3824ed611bb0c041d00c0805e84ceff28bf599c0751bbbdc862dc8bd079e7610bbaf29aa253de9d049df9cad2e893597fbc3eeb5fdfe14e045562e88c1d5dfb89a37663a1be2494ed9d8cef6afb260cfb3f6c5fc5e3e1d2f747e886b6731c16742f18bf57947f56d08d6c54474c7bcf3c00c09f64f41b3af9ac0545bbd89e70b2c3c5589c6beb1864412fda27af7c94385a79e5da775891a382ce51d47ff0956b2abbe98b718f757dd63f5daeff8b41259c45fcca2b457d7fc3c0197b7af6f3d75d5da9eef87b35ddded9f82eb56f4257702cb42f7c9951ed135b547378645f6bdceaf6ccf28fc6f7ecf8bda2fc51fb33dd363cbad1398f1ba367d59ea2bd00007f992ce8dc229ba342d9210751923e9b11a489f8b1d0e5f49c8b1dc572ed718bf856b7731ec56be1325d8445720ee2acd2755faeea6f6a83ef6bbc96c90e5b8498915e38a02040fc7571d4e2d0eafe45c1d338b9488842485db5231cd7bfd23fc18b90785dff5e2af0f04e32e513fb8a6009edb0ebd76334b65fd946f941fd87ffbc098095f1edb76f4ed93e5d6f60d63e115fcfa2feda7e7dfbeab24bc2f8edc9df1fdf917dc6e377befc58ce607ecdd60f6943f3bc54f8a868cc3bbb1700e04fa1237497421c8a120377440441ed7482602befd3f8888d88be9bf71d601fe3a83400004cb8aca073dc7181d751a0665b8d49740e000000e01057167400000000b000820e000000e0e620e8000000006e0e820e000000e0e620e8000000006e0e82eeefc1d62800f76765df3e00f8436441a73612ed6dbb616d8e7a6c41899b9c4e04c54f6d5b526c9efa4fbb1bbdec25f7ce8574567eb77d4bdba238dbcb3db201ecb00f718cd4c6af57e1a7edff16eae7af19c76a63e06a5ca4cd723db5bb785697857bb97170da5c79b46f6281ea8369bf59fa80d0bf7b6c63f43df347e6c3b6f9f8caf89b1b7fab74bf176031fe6e8db8e0f30f001d7484aefcc6278b7bb930d5df0865f15f5eec337103d18f49844816ff6547f442bc28328e2bba0a93f6c9980dc72446e6642ce76317c49f9d764fba477f5d0479a686a2258a76eb3eb9569cbf1cefd5f78c08652a21128fafdbfb8cd7eb44cd2cbdc715f6a5bccafcb16cb832fe4dfbe37a20fff76b825eef658d46d001dc87bea073a887bde6e8a22c8b8e771083b205db913c7cfefccdd22d36cf6291afbe618ae38b79a5bdf95b67ce5f2e6ee95b6e815ed0a611061140299f2b3b1e53b46ca749f9d3f6097111b7049f995f8f413c3b35a4c9385582ae48175bd6f5d4dff0c5c1e8f68dc72fb54ffaee8f4df3f7a8311ad827441f5c59295fcc9be786d8255d5714736c3abee7e6d70a4154d9698277bad266d7d6ba8d3eaff43f8d699c0bfa9e2e93e731d0efbf66b63674d3e3f9b9a17c1117657bd217c1e2192b6c356edf7c7e0deabfd4fc71cf617334e0daf82741678d41b8b67e941e005c8caea0f3dfcea3f3483727e242b1f7c1f7e5ebc5a6eb40c282552f66b260e9c3d1bdc050af619a6ff0c6e1fcda094879cdc26b2c8235d662280487a3edd75bd8c7f4caf74cdb57daa4210a81563888ad36db64f1a5eafa74cea61873b1af1375e97368f7e610e4b5aece3f1b3f8f778a619ef883f2dd3de9c0fc44dffee210dd78ab3ed411899508cb707c755b8eccaf09edb868427f427d8fa26e21e5cd652ccce58c21106b66fd4f0ce7afa39ffe28c65a04985e7fbc6897fad23dbefe6d3c97da379c5fe3fa852bcc9f5ef92be31fbef84441d994f10875a6f4e2590280cb530bbaf4b0fb6fa8c5c1e88972112daee7bc9aed5e2b42642eecb21035df40c3e257dca7913c8640cc8b9bfbbff4affe46ddd4bfe004cd05d5ca57943fb74fa2b7607b16da278bb629c485d8266d177dbdb857d725ff9fb65fc6684bf3af7f721d93f14b58eda8e8d9a771988e7acc0f3b64e9ff2be6d784665c34529e6a83dcabebcb79e3b83d16e64a66d6d685fe2786f3d7d14d777594f3a7167483f15d6ddfa89f93fa859f9f3fb28ed86d5819ffd4fed91809f5173200b838bd089dffd17cb3780da2767be92c7039ad23e8ea6840e6550ba6b108d67417ec3adfc8790c182eb60bedeb093a4b50e757ae535b2c0ab2c4e3e1faa11dc264fc120b36ebd9e7370a3a5de670fcaabc3edfe77cae6466f36aa1ff09d37e0a3b5d848a2b4b7d89acedf95e4137af5ff8e9f963961d5919ff95f66f884df6dc0f003fcae83774f2593b8c7631d9e9e4359d052e602f24e2d08af6f957169b7869167ce395c66cc19c3a36476b874051ff455fb9a6b25be728b67236cfd71ede41d4af4ce595ea96a744cad4515dffa5408df16cfc3c0b22a86f7f57fef495ab767aaeaee68bc3a8fc17ccaf09b500d9ca2ceb0a94fd2bf306dbee89b084f155edf5fddbfa34eb7fa267bf8499eee7b51b8b3436f1f76cda9efe75a1d4a7eed15f3a97dad71b9385fa859f9d3f8fa1c05a19ffbea073f7578276bc4603c0e5c882ce2d1ef95b7f5e048228499fcd0841b560aca0cbe92dfc7694c9b5c72d5a5bddd50254a58bf0498b972c96e9ba2f57f537b5c1f7355ecbe4054d163c23bd5860e3222ad765e19405bfd3bf9679f9e3f645a2e0ab9d4c426cefffd8c17044c1c1a4b2c5b9c436a9f9204e23d7edec2f0b7e2a479cc5b348776dd70e6c387e76ffb739b0621f294ff5a1a95f1cb538612b7d5efed9f935443f7f153ed2923f27a7addbebc641cd8d5c9f94b9f3f90c222596e5ecb3fa7ccded37b7afb661da3225f537ac1932beca1693f9b5d23ebdc68ceacff7fce0fc917b7a73c95c53abf12fd78fb25fa1fdee992f9e4fdd7600b83c3a4277296431baf9b74371084bcefc8504c166a709fe1bfa2ffde6dd4440007e0d4e0c32bf0160c465059de38e0b98fe16ec5fff19f7bc8d4974ee3753441f5454020000e04f70654107000000000b20e8000000006e0e820e000000e0e620e8000000006e0e820e000000e0e620e8fe1ebf79eb120000803f4916746a23cb44bded86defc32df73689fb5b809e74450fcd4b625c5e6b9b2d966b5af9becf5f6cefde566e577dbb7b46d89b3bddc231ba476ea7877ffd68873a4bb05c92cfd7ac8c9187eb3e92fdbbed6c6d1e57d327629cd8dfbee315acd1f6dbb53f0cfdb9f3856fecc7e7326fd97e747b5bddc1478217dca5faf5f7c48183fa9ffe7d718805f868ed0953b913f1ac75eef542ec262b489ad4ddc2053767c1f2de83fb5b1b02c5a5fc6290a5761d2bed9c6c23ebfb3ab8ce5feb1fb6ec4018d04db2cfda274c4747d2c53fdbcc973938f56f347cbed1bc3b5fc8bcfa7c1acfd81e3e567065f46464cfb1f9f8dfcb966963ee1afd7df8c9bfbbc67fe02c084bea0730c1e607bb19e9345e06471906ffbedc32e22531f4d23e72dea289e5ba454ba75b4ced7d32d2a397fe980440ca5bc192d184464c6eb76dfd5376029db7d1beddf6b30297fda3e41eceaae5982cfccafc760507f88be385bd74743ed89a0c6f331431d6e1ed4e7c25a478fe9fe4dd253ffa4ed3e9213db98c7b8c8efee6b367eaee68ffbbfd8723d7d91dab175a805528dcce9330ed1cabffa7cae60b5ff25e52fda6f46d3ff599b5e6013cd5fab5fc6be582f5edc1f803f4f57d08d2200f2207644c3085f7e7a80870fb373bc46383f3803b5908b831691111709696fb96004879fda29f57b071fef91f21ac710fb565cab28eca4f0f517df805b61b442af7ccfb47da54d1aa2336c165745bf7f5104e57cd2c7b1e82879a8bc627f2dda65acb6b1917ba58d9b609da547bc280df3ccd7e5e640aaf3d389f962cecafc70fd499f43bfb7b369e5b5b42e7f96becc8a209939bb83cf60c6c8effbb7f47c2e60e47f59f92bf69b61d94fae45b1ee29e6db42fa1efe60fdcd9a13dba0ef018013d4822e3fac3e02622d9ad1b15ad7f5c39ed9eeb52244e6c2ec178efab5a2d43b78f8258fe120f42222fd2b04aae518161699e0d817f215e5cfed9330cb4f2cb4cf8e6e46629b8e09ba364f63d311aeed45844cb7733616ab6365dd27c8bd85dd13dafe32c7b63479b55d3aac59fa22bd362a8676f5766ce7cd329dfccbcfe70256fb5f56fe82fd86acda4fe6cc4874ced27bfcd1fa1174006fa617a1f33f9a6f1efa41d46e2fa3c540d23a82aebca6e894771d41b78e597e62a17d3d41673954eb778abdfacf093a11b42ebffa9250e49d8dc5ea58756d1ee64f7bbdc3e3e1da3788c0cdd2474ce7453ff2e97f54eec6acfb1c4c58ce2fb6ed3d9f531622b767ca3ff85c09fbec379b333be794e32fd75f46e41da7e61800348c7e43279fb5c36f1dfdfe0525337c985db946e44a1684a27df1375169915879e55a2c28ab22a1a2b543a0a8ffa2af5c53d99638abefa9af9f7ae5eadbed447accfb88bfa7dbc643c6ca95a5ca1631be09a6597a64e0ece57e79656aa509e1cbca96d77fa951737496becc4c9098cf86b3b56bbffecbc4663ec7fb4224b81e97d5fc91eef3d92b5fd1cdab3853fecc7e26f3fecbf8eaf9f1e1d2f5fa374b0f1cb7ffefaedfe1c65cd72fcf6377fe01c07eb2a0738b648eda6427e91601e774d36733c233141736ba9cdec26c4799c2a2b4d55d467cea74113e695191c52b5df7e5aafea636f8bec66b99bc60895032d28b854b4447bc2ee243be0d77fad7322f7fdcbe88384ad7efde37f0f02d59ea3222a075d91e5dbfd8bbea6347145ae8311061e81d82169f51a0877b64b18f6d4a736c981eff5f51cea1877722395d1c94b35fb28388d367915ef66f963e63f5f9a91dad47c6b5ceeb689f9164876ddcf6e59f3d9f9df21566fb1547cb5fb59fc952ffabf563fa4733d67376c6febfb8fe88440873fee5b5110096d011ba4b2182abf9f6772f44b07cf7a215049b9d26f8889a2cee076ceb05dd0e0103000000dfc465059de38e024247d1ec6fb06f4484da203a7786223ab81a1501000080efe1ca820e000000001640d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d00100ec47f6db632f3500b80c85a0fbd8369fd43b82af222737f8cd74bfec4d23ad8d71cbfb3eca8d63772f96abf9e3e6973bf7629bb73f71ac7cc11fcde3ebb9a1b330366bbe23b297dfdef6a74d93477b00ce30378e16be613f46bde973c1e21635afe8ff15587ffee419af37e7bef9f30b00f7460bbaed2896871346f64edf4b748ee6a98f89aabfe1cabe73f968257f74d678c7f99ab5fce1faa788d7dd826edcfec0f1f21bbbb9cf777490b65d7e07f51cd048bfcf8e5753fec14da07bccda5f8c5bdcd750dff3eefebf9b51fbf73c7fe61cff25cf2f00dc945746e832f5c2d661b8b83ace3a082b7f16ad2f709456fbcf942f798b8d947797511dcde39c716a5f73dc921ba3f43947618aa3b5dc7dd5c6c8a90cc9ef23b13e6f6b03d3d909f1fcd650beb3939a63213ae5fa9fca8d65971b4b57fd73ffaf05478f14fdd2edca364965289b34ed97b188691a3dbffc7cfb54f74de6b7459a53b660e8db2f30b0cf62fb439d320f2abbbea2ff27e697941df294f363b9fc85f6af3f7f7674eefcf30b007082fa37748f875b4cc5717867208b5ebb704d591174b3c54ed29d53d95d77c2c8ef1d56aaf3ec626be43f5bbee9106ac73ac047068bfcc131271b6c0e3ba6cb38a9367e3a6759d85bf23ba7983f0b5ef4847efa83f6dd3ddb61fd81a69ecca3b8571cb876a822babc13cef748b47373daa1dcedec55391c7fd7599ec678343677f4dbbf092e2b4df2f9332c637952f6f439a8d0af5ddbbc63fbadd867defe587fc7ae67fa7f767ef9f2a5fe13e58fdabffafc053bb7e3ba9a1f00e02d684197a224f22df8d339860f27ee8a9b5711a1307164b2286a6754e016c253af7c3bf9734446316b670fabfd67cb3fe510e4dea96009afd293d32b04a0e4afda1ea8ecb838b6e63d7e5cb6b2e50b4329e8aafe3b4a3b8b40d5f937e7be823873e9bb9fe7ceae0f6f8ff64b43b7fd8e99a029e6c482ad6a52f9661b26f65bb1cfacfda14e29c79e7787fbff82f9f58af247ed5f7bfe9c6dea3647107400f0a3d4113a4116ceb018b6a26589a9232b232f1affa362f9766ea4adb09c5f165b4300add16f7fe640f975c46557199d7b6b27939da28c51717fdf89174cc736d4d1de238ed0b525fdc631dea7fb3b17740af76543c67a3942e708b608fdf4ff97d78386cdecf6070e0b9a45fae5cfed57d0b1cfacfdb3f61eeffff9f9f58af247ed5f79fe46363af5fc02009ca51574e238e2a27774419a3932b3dcf0fb1ffd97a9b6c37ab8f6b5dfbcd7f347ba7deb95af58b1cb91f25d1edd7e111dddf61bcc5eb906c2354b3c497dd3df4e2e8814d3e9893d9c33cdafcfe2efc174ff66af5ca5cdf98f5e1cf24a71d7fc74f73fe53758d236c9ebcb6bef1b396d6d37ffe5c18d63b26f33df166c55d3151c4bf69bdb67d6febabd757bcef4ffecfc7a45f9a3f6cf9f3f7976c7ebc299e71700e014af8cd059af1cad084ab3300be2b0eabc8eb67e119c92562daccbf9cb76b6cea353bec26cbfe24cf93eaad2cd3b2388da9cdfd9deaec3ddd3119be28452fe7f9cf31341101c5e6a77c966073b5df7739b5741b87df8cf9b83f5cef6c395935e1b8a988869215d04996e5f993ec5cf91549f38e7adee95f60b5e24a53455bfee9bb789bb2f7d5e1d47fdfb396d97c4dc7e73fbacb4bfe495fd3f3ebfce971fcbe9b43fa70f9e3f69c36c2ccf3dbf000027b0041dc04fa0a32700d742a2c5cc4f00b830083ab80245746ae1b750000000a040d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d001000000dc1c04dddfc36f162b7bb2997bd1010000c0edc8824e6dd499f0bbf6ab9badcd478f6d9e1937119d088a9fdaf7a9d89cf49f761361d938f89d9b86cecaefb64f445a7332448d6cdcebee910d562fb4f1699a5bb5addf479c836c91020000bf011da12b7742975dd74b6151ef942ec262bf038e1b747e4c224422307f2282e445913a0ee86a4cda2763361c93189993b1fc3ef1b4c62bdad43d3acb24885b3b0d0000e046f4059d63f05aaeb977912c0207650bb2d16cebdc4564eaa37de43c4b1dc5eb1f7d25edf5d7e42ccf9cbf74fefac8ae8c76f8e671431a1108299f2b3b1e03b46ca749f9d3f6095ef0d9513a337f1c8315fb8cecbb4695dffd5fca48e95ed0c981f9315dd79fda97e644ea4bb693f43be5531473c89f6d9bd2641ed682aedfbfb4f1b11e976c4fb1f792fd000000de4457d03d4224cd8c980c44c3085f7e1271434127afc35a6718c4a0123ae2a08bb32cab57b4d5e1f4dee98a938df748798d708a7d2bae5514765284c8a3b65f290056e995ef99b6afb449437cd52a7dafef99d96766df19a15fdbd99af2da570b525fbf8c7ba77e492fe663ec4bfeece847e8a4ad5bd9f9cb81aa7fd8bfce7cd5769cd90f0000e06dd482ce47163c9578ca44c7685dcf7935dbbd5684c87478e23c5dbe522804e75adca75974b833417058d059f98af2e7f64998e52716da674737231341d7b5cf827de7c8186e7d9757c79bc09a8fcfcaf875059d716f61cb85fe49d932277db4cee57b3851587f6198ce2f00008077d08bd0f91fcd37ce7110b5db4bc781e6b48ea02baf29161cf292c3d54ebe83b653c6ca77d0a19be52716dad71374a357aec2d03e0bf6ddc5e3e1ea6b2374a3f15919bf770abaf0ff300ffdffe5f5f0aafd000000dec9e83774f2b976f8a5839a44cd46741c6840a259ad63164152b4cf3bd74dbcacbc729d3adc05c1d4da2150d47fd157aea9ecc656316d649f997d67842f035b79fe4bc30e41a4edf2903faa91285f6527dd46118cdb170369ab8e083ebc285b7ee52ab8f63ce5377252a7b4cdf767bb7fd67e000080b791059d733e396a939d5c1025e9b319e1510e71155d4ecfe1d95126d71e71c2b96ee7808bd7c265ba089f240ac5d9a6ebbe5cd5dfd406dfd7782d930587082523bd109e2200e275110f2228961dfabcfc71fb2251f0f54496d8deff314015015db1cfc8be2b48f4ec598cdf26b096ea8f023ee735fee8c48bc4984f97dfe44f7f14e1ef53f37dd43fb16d16cb8f4238afd90f0000e04de808dda51087d88de0dd830fe7e4bfdb9907c166a709fe95e4303a0a000000b7e3b282ce61bd16bc3a3a8a566fccfc7626d139000000f8a55c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908babf075b97000000fc32b2a0531ba1f6b6ddd09ba7e67b0eedb31637749d088a9fdab6c49f2090fb58edfeefd3dfbb59ecacfc6efb96b62d91cd75dd3db201ef9bfaf06efb00000040858ed08960db1cf1a371cc657a1016a34d6c6de4f40939e1611221faa98d85bd28aacf91bd1093f6cd3616f6f99d5d652cf78fdd79ba67ad020000c071fa82ce31782dd7dcbb48168183b285ded15f4574eaebf3df6711c59b1ffdf5256771e6fca5b8103194f2665c19297d7e9cd3fc68aa2193f2a7ed13bce0b3a374667e3d06c5d158622b558e941baffb71c96d55f61fb55fe5d7fc84a8040000f87574059d3f5cbe13c519888611befc242086824e5ec9b6919c200695501001a204c5ec70752fea4468c57ba43c5378d422a9a2276643e451db6f879853f4caf74cdb57daa4c1892e295bfa5edff3e9c46e31a6623f27eab6cf22ca9420d7af7b15a3f613a10300007803b5a0dba2279578ca8848b29cb2088932fa12d8eeb52244a6e3f762a17ead18c459719f46f21802510b17e95f2140a2b8c99f85a960ea08162b5f51fedc3e897382ae17dd8cf4049db7f942fb9cc80bf7f54523820e0000e09be945e8fc8fe61be73b88daeda523c0725a47d075a3829df2ae23e8d67997a01bbf729d08e688cc0b11735f7e1ed86d44d00100007c33a3dfd0c967fd1babd651af8900938e000b4834ab75fc22488af6c5df7c25f1b2f2caf56d82ce51d47fd157aea9ecc6568ef00aba372621eff6ca3ad8de6ae7a8fdbadec7a7b37f23dc010000603759d0f9c84b8cda64c1104449fabcf4a3fc0574397dc76f45995c7b9ce8d8ea76e2a0881295e9227c9228f462245ef7e5aafea636f8bec66b191dc1aad33c5a780691e3af8bf011c1d2e95fcbbcfc71fb2251f0f54492d8fef921755942eae1455d2edbd95744b7dc37b7df8a7d5c3922a2535a1687000000700a1da1bb142218ba11bc7bf0318854bd8b20d8ec34814d850100007e219715740eebb5e0d5d151b47a63e6b73389ce010000c02fe5ca820e000000001640d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d05d0fb616010000805d6441a73716ee6cbba13797cdf71cda672d6e423b112c3fb56d49b1b9ae71007d7d62c5324bdb8ac8e6c4ee1ed980b753c7e1fa7711c7c8b5453ea7b11fed71777b9acd928d7b9629edb79fb3f967ccca7f77fdaf474e8ef19b797fd9e3676dcc5ddee7fa9cef71cffdee39b09a3fda76e717b679fb13c7ca9fd96f06f64f1c2b7fa39f5ffba6b4e97b7d0ffc617484ae3cb2494e0d28276c991e26d77e071f8f9dfa9844a07e6a63612fbade771cd56ce35f5fbf3ca8ced63f2f9e6481dc1cfa35daf47eea797e9cd27efb399b7fc6acfc77d7ff263a5f86ea7384eb719675299f4fec8feedb37dfd7f22fae7f06b3f6078e979f197c995c02fb9fb0ff20bff844f73c3e52904304f41f588f61077d41e718bcf6b327f39c2c0227af14e5db90f5309647537d3a87e3267f8ee2b98741a55b477f7d3ddd6293f3970fa888ad9437a31d9a3c50f1badd777180299f2bdb7d5b6dee1d44e9ccfab58d06f5876f8fce16f20d39e5756dd815e1f467dfc6bcf20d578e0853fd171b3e3f5dfb55f9da7ea3f1797dfb642c951dc5aef1ba9f37d956e5fc18cf9f8035b7d3fc4973328d5539b663fb0ddbbf927f86730245fefa5cde5ded6bd3759f7d2446eed37360d6bffaf974ff9767613d7d918ea0a8a91d748d9fef271ca6957f75fd5bc16aff4bca5fb45f17ec7fb8fc51feb63fd5f30bd01574a36f4832d9dc44da1bc5f2e5a7493a9cf06ea25667800a61b2570ed4399ee490fd371bed9cbd83d9da29f57b0714ef91f29a8527f6adb85651d84911be5969fb05e757de57b6b9212e86d2b6de3dfdfaa313cdf9a40de34573436cb5d9268b1f650b6f3f19978efde6e373a67d2ebf13e38d0072e56d9fddd8792152fd3fa6cfda97e8d957ae17cf43e1b8e6f61bb77f9e7fce43d956faabbf14cdca5facdff559c49e3cbbbe2ed78754e76c7c825db7d744f2b3827a7e8dd29729c6a5c370fd71c475a0e8cf1e8cfcbe7f4bebdf0246fe9795bf62bf11d8ff50f9d3fcf17908cf9b7b3eddfdf559d9f0c7a9055dfa761cbea15b0f655cf8adeb39af66bbd78a40990fbe4c6697af7c98a5dec1e2de7980c42925872dfdeb3be4889433aac7e11fbc957c9d854d844dd10e4dcca3db5d63d6ef6804ada3e9738f055b8ced37191fc7a9f6f939d1ce9f6641732222dc57d7356f5fa267df61ff67f69bb57f6afff9f325f71711323dcf66e54feb8f58f709b3fe79640cb634f969c3262057d217e9b551319c77de8ebadd3be9e45f5eff16b0daffb2f217ec3704fb1f2a7f25bfd49bd3dc97a5537682df472f42e77f94df4c9641d46e2fe2007adf6024ad23e8badfd83ae5dd49d0590fb4f53b42b37ec71504dde81bf5a9f6c5f2edb40d1fd59108929fa7ba3ff3f6257af61df67f6abf49fb17ec3fc6952f2256f5b968efacfcd5faadfb3c6be393793c5cfb0611b859fa886e1b13fdc8b0d429ebc8ca3cb158ce2fb6edad7f531622db67ca9fda6f02f63f59be6329ffce670e7e3fa3dfd0c9672d285a477762420d27ac38a7f68111c153b44f5ee92871d408069fbe39f1a1434e48bb267d6aed1028ea3ff8ca35956d899ffa9efafa77bc721dd96f3e3e67da17c4b9bc92b3d20469dff64a3dd4bda77df97ac7befafa437eb42cdf92f37d73fb8ddb3fcf3fc4cf5bf72528e697f6957d9b95bf58ffc0598ffb179e0f2db2fd9746b506ccd29719b4d163ae3d6e2ebaf6ebbf8c6ce67bbc2f444a9dadaaeb6bf923ddf5af57be62b87646ce943fb3df0cec7fae7ca1937f13aa0fffbc7df6fa077f932ce8dc4398a34279111787bb7d362348ab0e47a1cbe93df876142b3cb45bdd32a175fe325d84597a68e4e14ed77db9aabfa90dbeaff15a263f54cee1d5691efd608a538cd7c539cab7c5ba7ff2a0ba76f5be418a6dfc8fd15db9e53df3fabd08f43f648f6985835e200a9c50ae2c86b14e69ef82fd66e373ba7d7111d3e5cba227763adfbeb97d0bfbc4f195ffe7f207f69bb57f2dff186d0311cee13736ae8fc9c6b3f287e9f1ff15e5333aee9f88f767915e8eff2c7dc6eafa643a7a792eebbc8ee6be6c07352f84e5fcb3f5af53bec26cbfe268f9abf6eb81fd0367cbefe68f5f22fdf59dcf06fc117484ee52884336bea1dc0971a8f5032d0feb6831f0112b599c0ef47d14d5bb02576f1f0000c06db9aca073dc5100e8289f7fbda8d345a8b96fac65e4ed3514d1c51ddfaabf8babb70f0000e0d65c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908baeb7166eb12000000f8836441a737168ed4db6ee88d4bf33dcdc6892bc4cd152782e5a7b62d293647f51bacd6e907fbbdb46d89b38ddce3c6a357c7e1fa7711c7286e3192c67eb487deed313723de43b9b16edadc77b3599b9ee742fdfcd5f364969e372cddd2b73487f17cb71bf7ee685f9d3ea9df5a3b02db33ae3754cd146b44bf7d296f1ab7a2be852f462bf9cd8dc755bab4afd8d8fce99ee17a1c0000de858ed0c922b63932593cb705ae4d974570bc63b68d5bf444a8c9aed7a385561cc8c242fc72bce8aa4f69781d62d3a1cd62644e6cfdf3e22988cbf4f91a6d7a3ff53c5f459e87cf87ca178f7f4b36f3cf938c6dbadfd5530bb350c6f6b966969ebe3458f7d5e3e7cfbd55e33b6b5f699747b8bfb653a77e9d573f03d697b69efd67ed933a8bf3a7635b52fe192bf99ba3eae2f32afff7f655edf6c7c3eda81f00e0147d41e7508b554d6fd19d218ba65fcc07650bda116e881351df9cdd62f92c1c42f50dd9a5a7c557da9bbf35e7fce5e22c0e23e5cde805594529ecbe8b004af95cd9f5d150427412da8927ccfab58d06f587e881b3851c3796f2ba360c9d7f8d75f493ea7f7058aefdaafcc2b90dc6e76cfbd6f2f7c77f2d3d70686e4fe6731af7faba1637e1b39ecf2db3743f46529e9b2b751f725afa2cf7a4362fb4afb18bd1e751fd89bacf35a6fd17dae76d23f363d2a71e2bf993a0b3da18ae6d821300e05be90a3a1f5d281d40262e747b172e5fbe5e2c2b67b021af6f6ab110165c7d78b71720ea954df36ddf0b9432c2e04548bc47ca5b751c1a6b311742e451dbcf127e659b1ba2231c39ee7efdae3e790d95f3491b5a3bda88ad36db6471a66ce1ed27e3d2b1df7c7cceb46f9e7f36feb3f444cfbe43060246e89659e51b8dbb304e0ff608fd79347d0be317c5b0672b6ba57dc53de6fa30ae3f7144d0adb42fd926dbe888a09be40f5f2ce21782a63d0f9f377de128e72a00c09ba9059d5eec0be79c898edfba9ef36ab67b6521afd3cd455a165297af7406c1f916f7693a02312fceeeffd2bfc291584ef8a8a0b3f2759cbc3885ae438b7974bb6b7acead112c8ea6cf3d166c31b6df647c1ca7dae718e69f8dffc2fc48f4ec3ba433d6896e9955bed1b80bc374294bf551eed5b66d6ced056d286fa57d72cff6ec4a59d5fd93fab7ebef157469de3eac6772c04afef405a2db1e45fd4a1b00e0adf42274fe754c23dcac6fe507e938d89cd61174d6377e4fa73ced00a57f45db9533c8188b788db9985bf9acf21d3d4167095eeb77843d67720541d71d1fc7af1674b379d349afc54ddd9eda3ea3f4d9fcb16c5dd86fd23e6d176b7d589dbf759f6b743d9985f635cfbafc3c603426152bf9f74494c397dc3df703009c60f41b3af95c3b8472a19d4765ba741c6cc05e0865f12edae7230cdb82de38fcea959a5fa4b52379a5a07314f51f7ce59acab6c44b7d4f7d7df64a728cd8caddabf28a83ab5fb98eec371f9f33ed9be79f8dff2c3dd1b3ef0cb15791cf97bfd9ccdbc7cdf954dfca1f45d436efa75b7d29edd38c9fb4cf89aed5f6d57691cfdbfa30af3fa14598455d4f62d6bed236d21e27285d7aca3f63257f7fbe8a2d5dfee22707a3350e00e0c56441e79c73fe569d17315990b7cfb2a016dfbe8b7bd7d1e5f41ca7d4db2efaae3d6ed1ddeaae16d02a5d84997666e9ba2f57f537b5c1f7355ecb688755a779f4021f9d805c97855f7e605df72f0ac6d2f16d046727751911caa66e61abdf0b16ff870c314ddaa005cc8c28c042b9e2fc639dd2de05fbcdc6e76cfbe6f9fbe33f4f8f7d6dd8ecbbc2a3fea38d6a7e8a6848657b719ad2f4f35751dbbba6f84395fc6541f7c75d73edd27912e5fc5c6c9fb35bba3f3d33d3fae55ad387d2b6e6fa5208a27efbcc3545eacb6d1db392bf5c1f523f13d25ff7cca8f6f9f999d30100de8c8ed05d0a594c6ffeed562208b5a00b82adbc4fe3230007bfd93711a88b71b67d57ef1f0000c08ff1bffd2fff9b9d7001eee8c0f5b7f822c2214ca2736728a2078b5189efe46cfbaede3f0000801fe5fff87ffc1f760200000000dc83fff3fff97fda090000bf01898ca7e8ae09bf7503805fc07ff97ffd173b0100000000eec1fff5fffebfec0400000000b807fffbfff2bfdb0900000000700f9effebd34e00e870666b150000007803fff5bffed7f09f66d3cf76db0dbdb96cbea7da676d8db8e9e844108cb72d8965c42d2c52db467bbcfd16f4e6aab2c169dd67d9ebeed0b82c6dabe2ec2ef7b8f9726cecaf4bd85cf6f85639723286df4cfacbb67fb9316da0bc4f6c9bd2dcb8eeb6ef6afeb5e7af66defec4b1f205bf31b3afe7c8691d93fe377f1c51fd31c42c7dca5fafffecf801c029fedb7ffb6ff94379e48eecca5e2ed8657a1016fb0594ec2eef9ce6875b3c460bfed2c6c2b2806d7b9249fb7ebda09345f7ab3e45e275cc363ef6f5bb717997adfb472b7d0f2fd9fbb02376ebbed5cf93d49d0fbcf747c7edb3f15afec5e7cf60d6fec0f1f21bbbb9cf2fed7f9cbbf973cd2c7dc25fafffecf801c049fefb7fffeff943b3400f1e707b319f9345e064f1906840b318584753d5824e1f83d4880311a9fa689e4f973f38f0107d70ffaf8f6edae5dcfbe5a7f4d9d1645fcfb27eddfe1041aa50fd9705345db7c746d94fca8ec7413563eecab404a3597f1cc395f697e327f7aa7aa4de944f91e6408a0ee9b6e6f6641bf4edabef97327c24cde72ddb284e50c62bf52795bf4b40d78eadc34cbcfaf97cc2215af9579fbf15acf69f295ff216cfdbc93636fd9f95f7029b68fe5afdaf1e3f00d84957d08d2204f2a0ee75720e5f7e7ac0870fbb73fc4db85fc480bba6c491174fd99987f2fd6b82788fa4970240faa3c58bd4b32d42221abcc85075ec8918cdcbaf173ce9d36647df7ed5c7bafd214fb07d71ada2184785afbff8065f0aa440d9e6862856a46df53db3f67f3ab157cc19e9bfb377feece8dabb335f743b66f6f578d11be6a11f6777cf36de5b19e1d5e9d6975dac08ba99b33bf88c658cfcebcfdf0246feb3e537732af641dfb38cd17f7f2d89f424e6ebfa46e97bf883f5bf74fc00603fffe37ffc8ffcc13be4fc4057e224234ed23de8d675bd1864b67bad088fe9f8fcc252397fcb49560b86b4bf10a0459ee0dc739a4123081c5b99b3fe4dca97b61a0e4e2f82e3f647161649ef5857f2758487191d4dc43ccde2ed18b6df8fe9c87e819180963499133e5ae7faf270823b0bb605fb7a3a7d4e78912be5bbb28af9b787491d42632b8deb8bfd8c2dd2c9bffcfc2d60b5ff6cf92f1304abf6ebcc99cc2cbdc71fad1f4107f0c3d4822e2dc0fe47efcda23088daed65b45848da9b04ddc8498f05dd8c49f99dfede49d0590e5bffce71c5fe39adc348d0055b8572fcffe5f57aaa7fc1be9e4e9f13beeff2da78c521f698d4318afcfa1f95bb7e1c1593cbf93bf65a6321727da07c995fc5fc3950c63efbcde6e4da9cd5fce5fa5f317e007082fff93fff67fe500b01f95c3becd251ed5f7032c387dd955b456e425dee5a76ce0fefaceb57ae2341240b4ed13f2f0eb645c847674ebd729d955f898bea95e0acfd1eb1dbc4e6ed38058afa0fbe724d652f89dfc6fe9277bcc0eb72bd73d2c2de95e7c596942965bb7b757d33fb7a2c9b2a7219befcf5b12f98d461cf7d6753671ffd97898d3de37d21525cb76d357fa4fbfcf5ca57ac38ea23e5bb3cbafd325fbaed6f98f75fc656cfbf0f97aed7b7597ae0b8fd7f77fd8e53e30700a7c982ce39a11c75c982c12d12cee9a7cf668466222e2c74393dc7674689a2400a7965b110f111da208b572ad3e753fdd9ea088b5ebafe8f5bc0f26fca1cb2a07dfa3fb448e96ed1ea081b9b71f975ba08b3b428aeb4df8f45bc96c90b6eb445835e78abbe8960aaed2f8e58eca9af29c2b770a9ab8ca0aedadf8bf0785dec234ebf2847c4504eafec2f6dcb62531c4bfa7fa26fdf9e7df41c0bf33294b932476b569f8fdad17a7cdfdafccd7db91f95435dce3feb5ba77c85d97ec599f245c4f7f30e58ea7f353faa6d99e6e9c219fbffe2fa2387c70f00cea3237497420441f3edf0bd7841b74bc0dd1bf9065e2fbae28c47ceda472cc5797cf3d8000000c080cb0a3ac7770aac22fa6544557e0bba9fcd3770116a83e81c0000005c942b0b3a00000000580041070000007073107400000000370741070000007073107400000000370741077b61eb120000808b91059dda08b6b7ad85de3c36df7368f3c8b839e544108cb72d8965c42d4652db467ba8fd168acd79fd06cb75fac17159dab6c4d95dee910d80877594e3b38759ff0e636e760cabc8c9277e33ea2fdb7ed6c6d7e57d3277529a1bd7dd63b09a3fcebd9d5f38e6ed4f1c2b7f663f008053e8089d88a26da17934c2a04c0f8e77bfb37d04a1f63189f02c6d2c2c0bfc2618663bd8ff0abce8aaceb97d21b38d857dfd6e5cd66c5d8ecf126fee9f50cf63cd9ea3de7e824bb4af23e6ebb6d57696e7fe994e4ef147cfed7b5ed7f22fae2f06b3f6078e979f997e19020038405fd03906afd5464e7144168183b205f9b6dc2cd6d6d15fb5a09303dbd337ecc6f9894855d11f97f7298bf323d427657eca3768957fdfc6c6fdf2537a71b48e4b4fed93b6fb6b725669ce5fb65f1fa994d182691a8152f693b2e3313dcd98bb322d4165d6afc770323e65baf4b5ac67de3fb1674a93b1deea4ef64b73269565d9c19cbbd2ef54a7628fe0d075fa488c94518ce17cfcbbed5f69dfa27dfbed5b645190ccc4a77f5e77d8b7c6cabfbabeac60b5ff25e523e800e01d7405dde81bf4c0e98ff0e5a74570b8203ac7d49c1528ceca5d53e2c88b27e5f08353dcee9174bd7086c5582da4e20045c4c5fb45d47927a805d80e87372f5f8bbb98aeece8dbaffa58b73fe409b62fae5514e3a808918578fde0e1fcc91949dbca7be6e3f3e9c46a3167a4ffcedef9b3bf36eadf438d8dd8a71434d2ef62be761c67cf3ec29ef136f1a23acc73df56d7c7d4e695f19fb57fd4be25fb0edab74cc7ae0533c173700dc918f9fdb82ead2f0b18f95f56fe8afd0000f6520bbaf4ed3e4458ac45273a6eeb7aceabd9ee4d11028db9b0c962e9f2158bbdb508c6453d7d1e3bc4e03c739a41e3701d5b99b3fe4dcaef38002d8cc6ed8f547db6f08e67255fc7b198d1d144ccd308ba595be5ff43fb19796a5c5a1181aadab9643f87699f485f30cde7b7a753a7efd70bc6bfdbbe55fbf6dab78785329abe68fc38f6ecbc4027fff2fab280d5fe9795ff8a310000a8e945e8fc8fde9b457310b5db4bc7c1e5b43709ba5144602ce8664ccaeff4f74e82ce7268f995ebb4ad13c19be8f64f0495b395fa9251db6bc97e0ed33e915744e8ccb25f34fe43c1b962df5efbf6302da31fd97ec8ab5e6787d1733862397fc7de6b2c44e6cf94ff8a310000a819fd864e3eebdf48b58e70d189580c174471def5822a75b96b5970852851fdca75e410459014fdf3afbc36f172fe95ebacfc4a301e78e5d6173c1bed38058afa0fbe724d65b7e2773e3ef2f94bfdeecda4d73f7fdd89fc58fe23fe9e4edb4bf75bd2256265d9a1671f41f7cb8b87fa8bc58c81b35e19ff59fb47ed5bb2ef2bc4c4ac0cf3d97673c6b54fff65aaf4b798eff1be1009ad9fbbd5fc91eefad22b5fd1cdab3853fe2bc60000a0260b3ab7c8e4a84b76a8c1e9a7cf6684c672be137439bd85cd8c12458114f2ca622ee223b44116f754a6cfa7fab3d5119c42bafe8f384715f1f1ced2ff903fa56b81b2c2b8fc3a5d844b5af457daefc7225ecb64a7126dd1a01d4bd5371104b5fdbd70ea471a65ec82dd0da133189f70cfc38b8edc36671f718aa99c71ff4a1b89f0fef09f9548d2f5c7fef97b7d1f635b1a4ac7eb23d3296dd7f8dbe59773b83ffe9e61fbc33de3f68decbbd2be31abcfbf29b4645ed5791d6dfda99de5b8ace79fad2f9df21543a1e8385afeaafd00000ea123749742048df90df87db451a7df8d08a2da2105c156dea7f111cb9508060000007c1f9715748eef14584574e8177f6bd6fdacb7b59845e7000000e0a25c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b839083ad80b5b970000005c8c2ce8d446b6bd6d2df4c6aef99e6663cd15e2e69b134130deb6249611b718496d1beda1f65b28368ff51bf8d6e907c76569db126777b94736b81dd6518e0fbc8272e3e0fd829afce7f227d6d6af3e67f3030018e8089d88a2cd49cbe2e7163de5b4cbf4202cf60b28397d424e509844789636160ee2227d9eedf0fe2bf0a26be771543b986d2ceceb77e3b266eb727ce01cdee6d5f3b867be93ff5cfec0e2fad5e56c7e00800e7d41e718bc566bee5d248bc0c92bbbe5a3bf6a41f7e9ca8ddfc0fdd1483a7ff50d5da247cf18050c1beebafffbf331b7fcfb3636ee979fd267477f7d3daba39d72d9416ca5bc192d985494d51e1b653f29db385a6a14a533ebd76338189f95fe35f671ff97b66ce93326f98bf6497ad9cfd43fb1873f164dee8b6dd47df7f6d211ed54c789f2d33d23fcfc56cfc4de6790fce7f20babeb578fb3f90100ba74059d3fbcbd5c003303a73fc2979f16b1e182e61c637316a2384b774d89232f9e94c396f2fd198af11e49d70b76584cd5022e0e589d052aa2ce3b612dc07638dc79f95adcc5746547df7ed5c7bafd214fb07d71ada2e7a84264205e3f7838bf0819c9236d2bef591c9f41ff42bbb7b35de5b56e215827ccf27f3a3159cc59b1bf1beffc59f042cd95e3e6a69f07ee9e341f1abb8a2dd41c3e5bfe1cb1b1cb2be3e6ec286517f54d21ff99fc7efc97d62f9bb3f9010086d4822e471d9cd329c4494616454be4b8eb39af66bbd78af058c2c32f762e5fb1d88af3acefadc48db4bf10a0451e69f7581c3482cbb19539ebdfa4fcce02ae85d1b8fd91aacf16de71ace4b3ca7798d1d144ccd308bad3e323880d37db8ad3dd04e27c7e0df34b5b8a7c099ddfd1b149e0e1e77e9a97c57c7949f9135c1dda7e8fcf9d1164f29fcabfbc7e75389b1f0060482f42e77ff4de08b741d46e2f6e71ed7e4395b43709bad137f2b1a09b3129bfd3df3b093acb21e557aea7c7a7e2f170f7ef8bd01534f9c3f834f7d58cdae4c87d90fb8af17c4df9239644f400f29fcb5fd0799e97399b1f00a066f41b3af9ac7f23d50a85452766315cd05cb97564c3d7a5222e9d577a23c12082a4e89f7fe5b98997f3af5c67e5570ec5a76f2270d67e8f25cc2ada710a9411a563af5c53d9adf83d3f3ee1cb82b29fa4ef707ab3fcd21e79259b3e9b58362f0863d6f6ff55e5f709f653fd93cf4d598f18c96ce7ed3df2f7f99efafbf90bbaebd7d9fc000007c982ce399a1c75c94e3838fdf4d98cd04cc485852ea7e7dccc2851144821af2cce223e421b64714e65fa7caa3f5b1dae3f2232e2f57fc429ab05de3b69ff43fe94ee16e58eb0b119975fa78b304b8bfe4afbfd58c46b99ec14a22d1ab463a9faf669888b28187b914619bb60f72a822a9c1c1f11cfcfc27efbec3fcffff0a26b4b7702ccd92ff4c3b65f33071dbe2f1d67fe8af2fb54f3c76c43aa478f7be20ef947fc74fb03e3f5eb6c7e008083e808dda51087bf7bc13f871575f9cd7c3861523b9420d8cafb346c2a0c000070412e2be81cdf29b08ae8d781a8e35dd0fdacb7d59845e7000000e0a25c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b839083ad80b5b970000005c8c2ce8f4c6c29d6d2df4e6b0f99e431b63c6cd37278260bc6d492c236e3192dab66fa3d67b526c5eeb37f0add30f8ecbd2b625ceee728f9b2f4d1dc666c177c2dab8f98efd0000803f888ed08928da1cd8a31106657a1016fb05d42308b58f4984676963e1202ed26769dfaf17745e7419a734bc88d9c6c2e9c8a291adeb797217ea63deeeda0f0000f883f4059d63f05aeda8b3cb2270f2ca6ef9e8af5ad07dba725384a572d041a4aae896cbfb8c51c0109d71ff97e3b054fe7d1b1bf7cb4fe9c5d1432e3db54fdaeeaf3dcbfa75fbf5914119d5ff79844cd94fca96c3ebeb7b07513ab37e630cbb73a3183fe96b558f13f9c5f836e7a2d64737b97a74ff67e5efa4167800000097a52be8fce1ed9d28ccc0e98ff0e52701301474ce313767218ab376d79438f2e24939f4208ab67b245d0b8b2026b578917a36c125a2eedce1fcb3f2b5b88be9ca8ebefdaa8f75fb439e60fbe25a454f5085c868bc7ef0707e118d9247dad6bba75bbf13abc59c91fe3b7b6ff73c94ed8380d4f32f949bce46759f45c02a5bcccbdfc1e40b070000c0a5a8055d8a6e840858eb94b3b0b2aee7bc9aed5e2bc263397eef4c5dbec2394721d1dc5709ba42801679a4dd63e76efd666f2b73d6bf49f91d81a085d1b8fd91a382ceca6795ef30a3a3899867b7a0f3633ab25fb8a788b035ed101bebf44dfc2e95bf83662c000000ae4c2f42e77ff4de08b741d46e2fe2807b1110ef9cdf23e88a322bc6826ec6a4fc4e7fef24e8cebd729d085e4997c8a0fa1231b4fde3e1d275846e56fe1ef645660100007e9cd16fe8e4b376d8ada33ee1443b022720cebd76a852978ac8745eb98e04910892a27ffe95e7265ecebf729d955f0946e395eba8fd9ea382ce51d47ff0956b2adb12bff53df575192f79655a5ff7f87e39111fcb7cc4dfd3697b842f13cabe621f358786e5ef6138370100002e481674ce39e6a84b160cc1e9a7cf668466222e2c743996e317cc2851144821af3877111fa10d222252993e9feacf5687eb8f88c078fd1f11254a207891e2ffd022a56b01b9c2b8fc3a5d8459128b2bedf76311af65b2f088b668d082b4ea9bfc01486dff28187b914619bb60f72a82ba54ffc38bae9ce6ec23c2490bda9426c2fac37fde84a388eb6791bf1e9f71f9ab483b9ab907000070657484ee5288a0f9e628c928eaf41b11c1540bba20d8cafb343e6249040b0000e05a5c56d039be536015d1af0351c7bba0fbd96ceb3189ce010000c045b9b2a00300000080051074000000003707410700000070731074000000003707410700000070731074b017b62e010000b81859d0e98d853bdb5ae88d5ff33d9d8d81c7c44d68278260bc6d492c236e3192daf61736842d36cff51b2cd7e907c76569db12d99cd8dde3e6cbb1b19f73b8fd0000007f151da11351b43952d975bf74ac657a1016fb05543c36ea6312e159da5838888bf4f94fecf0ef45577d4ac3eb986d2c9c8ec5fa295b73c62a000080415fd03906afd59a7b17c92270f2ca6ef9e8af5ad07dba7253f4aa71fe2252f5d150725e688802860d77ddffe5382c957fdfc6c6fdf253faece8afaf6759bf6ebf88ad9437a3fa6f1f77a651f693b2e570fbfade4194ceac5f8f61313ed217558e941baffb71cd6d55f619b55fe5d7fc84a8040000b81c5d41e70f6fef4461064e7f842f3f0980a1a073c2e0e0e1fcfeecd0788fa46b6110c4a4162f52cf262844d49d3b9c7f56be1677315dd9d1b75ff5b16e7fc8136c5f5cab28c6511122a3f1fac1c3f94574491e695b7dcfa713a3c59c90fe397b6e9f4594c539a5ffaff3387aed1788d001000018d4826e8b7e54e2242322c472aa2204cae84960bbd78af0988edb3bfb4a1c4421d1dc5709ba422014798278ca6906d66ff6b63267fd9b942f6d3504ac1646e3f647aa3e5b9882c8ca6795ef30a3a3899ea0f36336b24fbaef23ded7178d66fb23083a000000835e84ceffe8bd719e83a8dd5e3a0227a7bd49d08da28a63413763527ea7bf771274e357ae73c12cc8bc1231f7e5e7515bb7bfc76a7f04410700006030fa0d9d7cd6bf916a1ded9a1337e9089c802bb78eecf8badcb52cb84294a87ee53a124422488afef9579e9b7839ffca75567e1dd50af64b2270d67ecf5141e728ea3ff8ca35956d895f198fafcfde9886bcdb2be5601bab9dbdf60bbade87ffbde3fbfe40040000e0366441e7232731ea92054370fae9b319a199880b0b5d4edf711b51a22890425e113f223e421bbc588865fa7caa3f5b1dae3f2202e3f57f441ca82891170bfe0f2d52ba16902b8ccbafd3459825b1b8d27e3f16f15a4647c8ea348f16a455df4410d5f68f82b1279264ec82dd2d21f5f0a22ed7edfa2fa25dee9bf76fa5fdae1c11b9296df7f8000000fc527484ee5288c3ef46f0de831575facd7c1891b020d8cafb343e62398cae020000c0b7735941e7f84e815544bf0e441def82ee67bd71f42c3a0700000017e5ca820e000000001640d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d05d0fb606010000805d6441a7367aed6d6ba13787cdf7d41bd32e1137919d08969fda17aed81cd76f605ca71fecf7d2b620b2f9afbb4736d0edd471b8fe3f803f3dc26fcdd23f6d6204f9ef9b5f4e66f19b65bbfc565e6b63eef23eb5f1b6abffb9bbfdabf9d7d6bf9a69fb657d29d2cb4db9e73c82fd52d9f5b646d5c6e17ce104b8183a42571eb9240f6fb96094e941f88c36a1b589c7467d4c22503fb0b1b0c78baef71d2735dbb8d7d7effa2db6de6fdbebf3d6b3586b11ec3eefb221f9ef9d3f519713a9e75ebd9ec9ba94cf17f647e3ed7b06d7f22fae7f06b3f6a7b5237fde49286fcb5f6f3ceed7a4faf391f10180f7d017748ec102d1dcbb48168193c547be8d5a8b6179b4d4a7fb46aca378f3a3b5be9e6eb1cff9cb0552c456ca9b7165a4747114e9badd77f50d5dca76df769b7bbd60b4a37466fdda4683fac3b777670bf50d5bdab027c299ea97b2f337f5c24673fba631d365f9bcd2ef984f538c7171b49bcbdb4408c6c8dc28fabbd3c191ffdef9331d415733fb727156b058f9a58ffedad1be299af6bfa04c8d29e8547fe4f38a9d01e09be80abad1375459383aa264842f3f2d38c3c5475e49b48b6d580cd502220240444c7402fe9b6fe11044206ced94faf5f99f525eb320c5be15d72a7a0b59f8e6adedd70aafd0b7aa9d9ae88c1ae7a6e8d71f4550ce276d183bad062f1ac338f9729c0d53792bf62de68be15847edf97462bb985352beeb8fbe678429082663a921ffbdf36756049d943d123fb1eebd6b5cc6c8bfbefe2d60e5976beacb925eebd609eb56f84255b74f9e77d70759675cd9f26c1eb60f00bc9e5ad0e5c5407e03521c2c9f9087da72ca2254525ecd76af158132175ebf30d5af3d837828eed3741648ed245604872f67e2444c4165e5eb38163bfa1889791ae7a630eb773482cbd1f47946cf19bec8be5d41e7c7bc9d1f7b7e07f4d38282fc3f9b3fd39bc38ae173e1eab5d7b8453af997d7bf05969e6bb1df51d1285f48dd7814ed937ea93ae5f78ec57801c0cfd28bd0f91fe5378bd2206ab797d16223691d41d7fd46d8294f3b8915c1e1cb9938116da78c95afe3587a826efaca3562d6efb8b5a09b09f605c47e45fd9d36f720ffbdf3677a7338d38f5cfb3fca70751e8d3c2de73fda37cf6ae4fde433256d54f91bc13db533007c2ba3dfd0c9672d285a217162c1182e68ae5cf3956bf98d56feaa4d7e73959cc02b5e09d68b98456b874051ffc157aea96c4b9cd5f7d4d75ff5cad52a5b58b16fcafb901f7d1b11085d86777e4ab88bc3d03fcade8d1b3bfda36d29af186fcf2346920dbbdc22ff80bfdeffc44c68b87adab5c73d2b529fcad7ac17f13ebbfdabf923661b84817d129dbcf26cd57fd4d07e21ec972ff9b7b7320f677f779fca1ffa53f56f646700f85eb2a0738b608e0a6541134449faec1ff0744f73ef3aba9cbe78a8bead7bc2a2b9d5edc441f15ab84c176196162d597cd2755faeea6f6a83ef6bbc96c90b9a083123bd581883c0f4d745488960a9fb278bb1124135214a21751911caa66e61abdf8b25c9abdbd011852d76f9e518f4edeb8902dba7c7fefbfbb49313671bf3b7ed1327528eaf38ae9ead2c4424a6fcf6dc4afddceca6b97efe317fb9ffabeb9329b4e4b9acf33ada35a8d3fee5fcb3f56f6c1fa12f14abe7d3fc8ddba0fcf89a75cb5f3f7b6d7a911f007e161da1bb1422b86ebe60d47f25e6af79c156dea7f111b5eeb7f7314d040d000000fe069715748e3b0a141de56bbe214fa2736728a28b4654020000007e3157167400000000b000820e000000e0e620e8000000006e0e820e000000e0e620e8000000006e0e82ee7a9cd9ba04000000fe2059d0e98d8523f5b61b7a73de7ccfcecd3f037173cb8960f9a96d4b8acd6dfd99b675fac17e2f6d5be26c23f7c806bc751dc666c81687dbf7db59b4df1964635c39bc5ce60d63000000df868ed08960db9cd0a31106657a103ea34d726d1e41a8c9d1502341f7531b0b7bd1559fd2f03a661b0bfbfa5dbffbbbc1b7e300fb789bfd6a11ee3eef7f3e0000000ed017748ec16bbfa34e318bc0c92bc5ded15fe5d1509fff3e8b285eff682a69afbff6ac8e9eca6507b195f266f426bdd3088f44d7523e57763cc6a8b169274a67d66fd8a86bfb41fb56fa5f1cdde5efddbb09f27bc7a7c9effe2f6574d355f99aaefd4ef65ffa5e449427731c0000e06574059d3f5cbe13251a889211befce4e086ce4e5ec9d6ce3c89412d8ee4becd8936af688dc3e3f5f9a1525ee3d863df8a6b153d4110228fda7e4e1434f7956d6e88519e461c28ba8224d24b9ff5ffd389a9624cc57e4ed4e4cf13de3d3ea15fdbf992f25a5a0bee59f9899e7d5ed1ffb2fef95c0200007809b5a04bd189f0dbb1d6e98528542bb68250d9a21b1bdbbd5604ca1426e2085dbed21107e75cdca7e90844ed64a57f85408de2297f168e0a3a2b9f55bec38e3e46de2ce8bafdf7366fc767744878c9778c8fd4b1b54d5e8de7c3fd17ca4f74c7afe87762b5ff083a0000f8417a113affa3fc46b80da2767be938e09cd6117475b425b3e0d0e782c1b1e084bb82e0a4a03bfdca35d24b1ff77f22c8a67cd3f8241e0f77bf8ad02d949fb0ed73b6ff61fc8af677da040000f07246bfa193cf5a50b48ef084131c3a3b57ae1119118759b4cfbf52db9ce8ca2bbda960b08459852d08aafa0fbe724d65377d31eeb1d2845efaacff227ee495664edfc9bbc7277c9950e54bba9a43b3f2133dfb9cedbfcc9d6765cfa23f000000ef220b3ae71c7354280b9a204ad267338234113f16ba1ccbb10a7614cbb5c739c9ad6ee7c08bd7c265ba08b3240ac589a7ebbe5cd5dfd406dfd7782d930583083123bd109e41c0f8eb12dd942d2ceafe45c1d88b6485288fd46544289bba8554ff387da5ff623f1121e9bad8570453afad2def1e9fe7bfcfa27cd7b742f4f6cb5f1bbfc7c9fe4b3f2cbb020000bc191da1bb14e2d0bb11bc7bf061448282602befd3b0a930000000ece6b282ce317aed78557494afd9f662129d0300000038c495051d000000002c80a003000000b839083a000000809b83a003000000b839083a000000809b83a083bdb0b50a0000c0c5c8824e6f2cdcd976436ffe9aef39b4796adce47522087e6adb926273597fa66d9dfea64d63cdcd7e5bde56ffd2b62a6eece41ed7d6b7b4e136c439ec6c61a71fe76de30b0000bf171da12b8f44925df34bc752a607e133da24d7e61184dac724c2f3531b0b7b51539fd2f0bdd476fe4e661b1f7bfbb8719136ee1ffbdf4610b7761a0000c037d217748ec16bb5a3a2238bc0c92bbbded15fe5d14c9fcea1ea28defc68a9afe7f3dfaf9c5f1ffb14c44cca9bd10e7b1a4113079ff2b9b2e331507bedd4b5eda0fe95fe85b34d639abfd788c60da274a67de218860d95dd58c8716729cdd55f44589d88dfea77f3a03937b51a3ff77f69cb4a7adad059db25b73797313a1a6cbb5fcaf0c7b6f9bcca8685fd641eef1574ae5c557f337fa7f36b6e9fa3cf070000dc9caea0f387cb77a23003a73fc2979f44dc50d03947599cb119086250393a71b0eaa0fbd9e1ec5ef488838ef748798de38c7d2bae55f40457883c6afbf51cf3985ef9895efaac7f9f4eec156326f671a22e7ff694366d88af5aa5ecfa1edf5f1189f9bad8408fe343a50501a5e757e8d77676aabcd6d5827a98de994fba9db3f9e1f1a2cad5e3caf26d75f78436cbbd9b6db3789acc158ddc3f9abf89d1f88eec332b7fa9ff0000704f6a4197bfdd3b475038874c746cd6f59c57b3dd6b45784ce122ced9e52b1d4d703ec57d9a05872efd2b046a1427f9b37054d059f98af2e7f649f41c7aa2973eec9fb7e95afd76743412cbd4764d3482c151b4c9b5a1881036f5c818eb742da0e6e9221e65cef8689d172a0f3f67fc3c5a981f1e6b4ef4ae2fcc958dc9fc55f4c77fd4ff49f9abfd0700807bd28bd0f91fbd37c26d10b5db4bc7c1e4b48ea0eb4613161cd650f024169cb4e970ad7c3d7130a1efd0c7e9e3fead0b8a9ea01bbd7215c682ced52fd122f525a169afe6f170e965046a961ec63af4d3ffff53cd8985f9e1e98dd9c1b9b23199bf8ad9f87b9afe9f7f3e0000e0c68c7e43279fb5c36e1dcdba4868e838988038ff36722482a2689f77de9bf898bd526a04c42b059da3a8ff875eb98efa27ce5b5ed9e57413b17ddfc9a7ba2df1367ce5eaedea447a4c7bc4dfd3e9f6862f0b5b7bfd970a354766e9d2dfa7fc8650fa2c69fefe98e698cd0f4f65b30db95747c41ede9e5dc169309bbff9fa607e8dfa7ff6f90000801b93059d730e39ea929d541025e9b319a1d9e1d012ba1cdb798af3b1a244ae3de24473ddce4115af85cb74116649148a934cd77db9aabfa90dbeaff15a263b4ce7fcea348f169ec181faebe2fce587f59dfeb5ccca1fa7aff42f8b90785dec2782a070e851d0f69cbc8c9dff638026821a0583ff438154be1640651b45f87df8cf9bc810f1f72cda57e69fa5fbb6e7f21e8630edcf8f9e7d8b3918055248137115f32c3f03a3f96bd7afe7d7b4ffc3f2dbf4b2ff0000706b7484ee528820d1d1971b2282655dd05d8320d8ec34c147dc3ad1d5260204000000dfc365059de38e024147f9cc6d41aecc243a37a2886e2e47ac000000e0255c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b839083ad8cb68eb12000000f801b2a0d31b0b47ea6d37f4c6b0f99e43fbacc54d542782e0a7b62d2936dff51bc8d6e947fb3dc1dc0cb8e56df52f6d5b229bebba7b5c5b9b362cb6ff2c6feb3f0000c05dd111baf2c8a147e338cbf4207c469bd0da3c8250fb9844787e6a63612f6ada5310be93dacedfc96c63616f1f372ed2c6de7d3fd97e0000803f495fd03906afd58e3aed2c0227afec7a477f954757c979a03a8ad73fda48daebafc9599f39ff76ac92206226e5cde84d72a71128753494942d87a777efedd3b5eda0fe95fe954757c9bd46346e10a533ed638c617f6ecc8e9e9ad8ef6cff0100007e2b5d41e70f97ef4461064e7f842f3f0980a1a09357b2ad330e625039721128c559a0d52bdaeaf071eff4c5c9c77ba4bc4678c4be15d72a7a8225441eb5fd5ae1b142affc442f7dd6bf4f27768a3113fb3851973f7b4a9b36385125654ad9bd7b86f6198ccfaafd8ef61f0000e0d7520bba140109bf1db39ca138612bf2214220e5d56cf75a111ed3e18aa86a0e7f0fcebfb84fd311885a7848ff0a811ac549fe2c1c157456bea2fcb97d123dc192e8a50ffbe76dba56bf1d1d8d1c1574b3f199da6fe350ff0100007e33bd089dffd17b23dc0651bbbd741c7c4eeb08baf29a622618dcff971cbe252c2aba82655190cce80996442f7ddcbf892056f404dda957aeb3f1d961bf63fd070000f8c58c7e43279fb5c36e1de9ba4868e838f88044b3dac891088aa27df13761c989afbc727d9ba07314f5ffd02bd751ff443c7d7df66c9e10db577654a4ba1b5b1bf7d4d767e3b36abfa3fd070000f8b56441e79c5f8eba6441139c6afa3cfda3814574393d876b47895c7b9c28d9ea7602a0782d5ca68b304ba2509c7dbaeecb55fd4d6df07d8dd73259748ad031d20be11904a6bf2ed1cdcf3d826256fe387da57f621f1175e9bad84f447511f58c82b6170995b17b7e485b8c086a2ab740dba73f3eb98caefd5ed17f0000805f8a8ed05d0a71c8dd08de3df8e84492ae4c106c769af09d9b0adfd17e0000003fc265059d63f45aefaae8289fb92dc8959944e7be835bdb0f0000e0a7b8b2a00300000080051074000000003707410700000070731074000000003707410700000070731074b097efdcba0400000016c8824e6f2cdcd936426fde9aef39b44f58dc247622087e6adb9262f35d7fa66d9dfea6cd6a1737c37d5bfd4bdb96c8e6bfee1ed7d6a60d3fddfe1bf090cd92fdd62c67f6d85b7b7e0000e00fa1237422d8362723a70a948eb74c0fc267b409ad8d9c3e21273c4c223c3fb5b1b01735f52908df4b6de7ef64b6b1b0b78f1b176963efbe9f6cff597cf4d1b8fe126a11ec3ebfedf9010080bf455fd03906afd58e3aed2c0227afec7a477f9547577dfefb2ca278f3a3bfbe9ecea9e6fca5f3163193f2665c19297d1e816a8faeeadfdba76bdb41fd2bfd0b67a7c6347faf118d1b44e94cfb1863f8a3edefa0dbeeeb566df1632cfd4e9f15790ee6f46dbea536af1e7f2773b788384f9e018bd5e7070000fe185d41e70f47ef4461064e7f842f3f39a1a14392574a6da42438332504c4c12b07dbbca2350ee7f72221de23e535c223f6adb85651d849112227da7ead7059a1577ea2973eebdfa7134bc598897d9c28ca9f3da54d1b9c1092321b71a2f8d9f6f769ea95be54737018a18b7ddfaec9180feeaf686cb630d734befd4bcf0f0000fc396a41b745272af1941191643931110229af66bbd78af0988e5f9c9575f8fbc8f9751c9c76a2d2bf42a0360eda7154d059f98af2e7f64998e52b7ae9c3fe799baed56f474723b1cc7709ba57b4bfcfc3cfdd34af9a2f00fedaa8bc4ac059f367c05941b7fcfc0000c0dfa317a1f33f7a6f9cdb206ab7177166bd088377deb6a02baf293ae55d47d0add3133c895efab87f1341ace809bad3af5c23ef6eff885c87946db47d1a71cb6dda179d13c47e45ff46cfc08c33790100e0f731fa0d9d7cd60ebb75c4279cecd0214934ab7596e2108bf6c5df542527b9f2cab52f182247059da3a8ff875eb98efa27e2f6eb732602c4f66de42a91eab6a25bf53d569af0def6cf0873a2d77e7dddff456af3c52208b94f491bf4d1c4cdad67d59fa2bf7b40d0010080260b3ae73c73d4250b9a204ad267334233113f16ba9c9e53b4a344ae3dce096e753be75bbc162ed3459825512862215df7e5aafea636f8bec66b99ec3445e818e985f00c02d35f97e8cd2ea73f2b7f9cbed23fb18f8888745dec27a2a0102c51d0f622a121ca246d3122a8a9dc826f6eff02beae8e18f291e95cbe6b9b255a7ddb52bff62122b1edd73e569e1f0000f863e808dda510a779f308c487130e7773b841b0d969827fcdf8d7a34322fa1052000070252e2be81ca3d77a574547f9f66cab710926d1b9bf4e19a1bedfdc0400805fcc95051d000000002c80a003000000b839083a000000809b83a003000000b839083a000000809b83a083bdb075090000c0c5c8824e6de49aa8b7ddd09bbfe67b0eedc71537999d08829fdab6a4d8bcd69f695ba7bf6943573506a3f2df56ffd2b6256eece49e37eec5f6b6fe010000fc5674844e04dbe6486557fed2b196e941f88c36a1b589c7467d4c223c3fb5b1b01735f52908df4b6de7ef44c67c38a63132276ddc3ff6e7d97b7e2a0000c09fa02fe81c83d76a4745471681935776bda3bfcaa39f3eff7d1651bcf9d15f5fcfea68a75c761033296fc69591d2e71134895ea57caeec78ccd35e3b756d3ba87fa57fe16cdb98e6ef35a27183289d691f3d86a3f2a5dc78dd8f6bee8b1abf917d557ecd4f884a000080cbd11574fe70f94e1466e0f447f8f29300180a3a7925db46628218548e5e04841204cd2b5ae3707e7d3ea794670a072de20c7a822b441eb5fd0c61b240affc442f7dd6bf4f27f68a3113fb38d1953f7b4a9b3638d125654ad9f53dd3f2bd285382de789d2d8cfa4f840e0000c0a016745bf4a3124f1911499653152150464f02dbbd5684c774dcded91b87bf8f845647206ae121fd2b04441427f9b37054d059f98af2e7f6498c048dd04b1ff6cfdb74ad7e3b3a1ae909bad5f29dc80bf7f545e3a8ff083a000000835e84ceffe8bd719e83a8dd5e3a022ca775045d794dd129ef3a826e9d91a0117ae9e3fe4d04b1a227e8c6af5cd7ca97792562eecbcf23bb8fa3fe23e80000000c46bfa193cffa3752ada35d17090d2341e7a359ade3164151b4cfbf52ddc4c7ca2bd7b7093a4751ff0fbd721df52fbc82edd93c21b69f47cf1a5b3b66e54bdeed957018bb5e3f7afdd7f53e3e5dff1ae10f0000f007c982ce474e62d4250b9a204ad2e7e91f0d2ca2cbe93b6e2b4ae4dae344c356b773ee4594a74c17619644a11713f1ba2f57f537b5c1f7355ecbe808549de6d1c23388147f5d848b088e4eff5a66e58fd357fa27f611d195ae8bfd44541782280ada9e4892b17b7e485b2c21d52f7fdebe15fbba7244a4a6b42c0e010000fe383a427729c4e1772378f7e0631069ba2a41b0d969029b0a0300005c90cb0a3a87f55aefeae8289fb92dc8959944e7000000e0a25c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908baebc1d620000000b08b2ce8d446afbd6d37f4e6b0f99e43fbacc54d642782e5a7b62d2936c7350e90af4fac5866695b10671bb94736d0adeb6836e355698ac3edfb05f8d323fcd63167f6005c9b9f000000974147e8ca239764d7ff521894e941f88c36a1b589c7467d4c22503fb5b1b0175def3b4e6ab671afafdff55b6cddbbaf1e873bf1d6b3586b11ec3ebf6d7e0200005c89bea0730c5efb1d151559044e5e29f68efe2a8f96fafcf75944f1e6477f7d3daba3a372d9416ca5bc1957464a9f47c824ba96f2b9b23fc3fd8d4d3b513ab37ec3465ddb0fdab7d2ff70f66d4cf3f7ae6f32acdbeeeb566df136947ea7cf8a3cc6397d1bcfd4e6d5e3e5646e1411ddc91cb3589d9f00000097a22be8fce1f29d28d140948cf0e52727397498f2caab8de40467abc591dcb739f1e615ad7138bf3eff53ca6b8451ec5b71ada2b093224476b4fd5a6115fa56b55313a34c8d3851f4ea4ff4d267fdff7462af1853b19f1375f9f384a65ee94b35c6c3085decfb764d6c38b8bfa2b1d9c2586a7cfb97e6270000c0c5a805dd163da9c453464492e56445a8a4bc9aed5e2b02650a1371a6cde1ef419c15f7693a0e583b79e95f21501b01e1382ae8ac7c56f90e3bfa1879b3a0ebf6dfdbbc1d9ffa70fc310f3f37d2b83502db5f1b955709b88efd7a9c1574cbf3130000e06af42274fe47f98df31d44edf6d2116039ad23e8ca6b8a4e79771274a75fb9467ae9e3fe4f04f322b90e29db68fb34e296dbb42f3a2788fd8afe8de6d88c3379010000be9bd16fe8e4b31614ad50382102860e53a27dad3317875db42ffee62b39f19557ae7d4113392ae81c45fd075fb9a6b2ade8567d8f9526f4d267fd17f1fbf57956c4049bf7daafaffbbf486d847b10729f9236e8a3891bbb67d59fa2bf7b40d00100c09dc882ce39f71c15ca82268892f4d98c204dc48f852ea7e7b4ed28966b8f73d25bdd4e1c14af85cb74116649148a9849d77db9aabfa90dbeaff15a263b751162467a213c83c0f4d725ba64899228187b91c6106592ba8c086553b790ea1fa7aff45fec2722285d17fb8aa8e946453bf8ba3a62c8477e73f9ae6d9668f56dd3765d474462dbaf7daccc4f0000804ba1237497429cfacd23241f46a42c08b6f23e8d7fcdf8d7a34322fa1052000000eb5c56d03946af1daf8a8ef235db7e4ca2737f9d32027cbfb1070000f831ae2ce8000000006001041d000000c0cd41d001000000dc1c041d000000c0cd41d001000000dc1c041dec85ad550000002e4616746aa3d944bded86de9c36df7368bfb0b809ee4410fcd4b625c5e6bafe4cdb3afd4d1bceaa311895ffb6fa97b65591cd93dd3d7f7eafb838879d2decf4e3bc6d7c0100e0f7a2237422d8364722a706948ea54c0fc267b449ae4d3cd6ea6312e1f9a98d85bda8a94f69f85e6a3b7f2732e6c3318d913969e3feb1ff6d04716ba70100007c23fff93fffe7fca1111283d76a4745471681935776bda3bfcaa3a93e9d43d551bcf9d15f5fcfeae8a95c761033296f463bec69044d1c7ccae7ca8ec750edb553d7b683fa57fa17ceb68d69fe5e231a3788d299f689631836547663e1cf678d69aefe22c2ea44fc56bf9b07cdb9b1d5f8b9ff4b5b56d2d386ceda2eb9bdb98cfefcd0f74b19fed8369f57d9b0b09fcce3bd82ce95abea6fe6ef747ecded73f4f90000809bf3fffbfffcaff9432124fce1f29d28ccc0e98ff0e52711371474ce511a67790631a81c9d385875a240f38ad6389cdf3be8788f94d738ced8b7e25a454f7085c8a3b65fcf318fe9959fe8a5cffaf7e9c45e3166621f27eaf2674f69d386f8aa55caaeeff1fd159198af8b0df4383e545a10507a7e857e6d67c7ca6b5d2da887e99df9a4db399b1f1e2faa5c3dae2cdf56774f68b3dcbbd9368ba7c95cd1c8fda3f99b188defc83eb3f297fa0f0000f7e4bfff97ff6ffee00541fa76ef1c41e11c32d1b159d7735ecd76af15e131858b386797af7434c1f914f769161cbaf4af10a8519ce4cfc2514167e52bca9fdb27d173e8895efab07fdea66bf5dbd1d1482c53db35d1080647d126d7862242d8d42363acd3b5809aa78b789439e3a3755ea83cfc9cf1f368617e78ac39d1bbbe30573626f357d11fff51ff27e5aff61f0000eec9fffcff3ff307ed48fc8fde1be13688daeda5e360725a47d075a3090b0e6b2878120b4eda74b856be9e3898d077e8e3f471ffd605454fd08d5eb90a6341e7ea976891fa92d0b457f378b8f43202354b0f631dfae9ffffa9e6c4c2fcf0f4c6ece05cd998cc5fc56cfc3d4dffcf3f1f000070637a822e7dd60ebb7534eb22a1a1e36002e2fcdbc891088aa27dde796fe263f64aa91110af14748ea2fe1f7ae53aea9f386f796597d34dc4f67d279feab6c4dbf095abb7ab13e931ed117f4fa7db1bbe2c6cedf55f2ad41c99a54b7f9ff21b42e9b3a4f9fb639a63363f3c95cd36e45e1d117b787b7605a7c16cfee6eb83f935eaffd9e70300006e4c1674ce39e4a84b76524194a4cf66846687434be8726ce729cec78a12b9f68813cd753b0755bc162ed345982551284e325df7e5aafea636f8bec66b99ec309df3abd33c5a780607eaaf8bf3971fd677fad7322b7f9cbed2bf2c42e275b19f0882c2a14741db73f23276fe8f019a086a140cfe0f0552f95a00956d14e1f7e13f6f2243c4dfb3685f997f96eedb9ecb7b18c2b43f3f7af62de6601448214dc455ccb3fc0c8ce6af5dbf9e5fd3fe0fcb6fd3cbfe0300c0add111ba4b218244475f6e8808967541770d8260b3d3041f71eb44579b08100000007c0f9715748e3b0a041de533b705b93293e8dc8822bab91cb1020000809770654107000000000b20e8000000006e0e820e000000e0e620e8000000006e0e820e000000e0e620e8602fa3ad4b000000e007c8824e6f2c1ca9b7ddd01bc3e67b0eedb31637519d08829fdab6a4d87cd76f205ba71fedf7047333e096b7d5bfb46d896caeebee716d6ddab0d8feb3bcadff000000774547e8ca23871e8de32cd383f0196d426bf30842ed6312e1f9a98d85bda8694f41f84e6a3b7f27b38d85bd7ddcb8481b7bf7fd64fb010000fe247d41e718bc563beab4b3089cbcb2eb1dfd551e5d25e781ea285eff682369afbf26677de6fcdbb14a8288999437a337c99d46a0d4d15052b61c9edebdb74fd7b683fa57fa571e5d25f71ad1b84194ceb48f3186fdb9313b7a6a62bfb3fd070000f8ad74059d3f5cbe13851938fd11befc240086824e5ec9b6ce388841e5c845a014678156af68abc3c7bdd317271fef91f21ae111fb565cabe809961079d4f66b85c70abdf213bdf459ff3e9dd829c64cece3445dfeec296ddae04495942965f7ee19da67303eabf63bda7f0000805f4b2de8520424fc76cc7286e284adc88708819457b3dd6b45784c872ba2aa39fc3d38ffe23e4d47206ae121fd2b046a1427f9b37054d059f98af2e7f649f4044ba2973eec9fb7e95afd7674347254d0cdc6676abf8d43fd070000f8cdf42274fe47ef8d701b44edf6d271f039ad23e8ca6b8a996070ff5f72f896b0a8e80a96454132a3275812bdf471ff268258d11374a75eb9cec66787fd8ef51f0000e01733fa0d9d7cd60ebb75a4eb22a1a1e3e00312cd6a234722288af6c5df842527bef2caf56d82ce51d4ff43af5c47fd13f1f4f5d9b379426c5fd95191ea6e6c6ddc535f9f8dcfaafd8ef61f0000e0d792059d737e39ea92054d70aae9f3f48f0616d1e5f41cae1d2572ed71a264abdb0980e2b570992ec22c894271f6e9ba2f57f537b5c1f7355ecb64d12942c7482f84671098feba44373ff7088a59f9e3f495fe897d44d4a5eb623f11d545d4330ada5e2454c6eef9216d3122a8a9dc026d9ffef8e432baf67b45ff0100007e293a427729c421772378f7e0a31349ba3241b0d969c2776e2a7c47fb010000fc089715748ed16bbdaba2a37ce6b6205766129dfb0e6e6d3f0000809fe2ca820e000000001640d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d0c15ebe73eb1200000058200b3abdb17067db08bd796bbee7d03e617193d88920f8a96d4b8acd77fd99b675fa9b36ab5ddc0cf76df52f6d5b229bffba7b5c5bdfb547dcdbfa070000f05bd1113a116c9b239553054ac75aa607e133da84d6464e9f90131e26119e9fda58d88b9afa1484efa5b6f37732db58d8dbc78d8bb471ffd89fc747078deb0000007f9abea0730c5eab1d151d59044e5ed9f58efe2a8faefafcf75944f1e6477f7d3d9fff7ee5fca538103193f2665c19297d1e416b8faeeadfdba76bdb41fd2bfd0b67a7c6347faf118d1b44e94cfbe8311c952fe5c6eb7e5c735fd4f88decabf26b7e42540200005c8eaea0f387a377a23003a73fc2979f04c050d0c92bd9361213c4a072f422209420685ed11a87f37b9113ef91f24ce1a0459c414f7085c8a3b69f214c16e8959fe8a5cffaf7e9c45e3166621f27baf2674f69d30627baa44c29bbbe675abe17654ad01bafb38551ff89d001000018d4826e8b7e54e2292322c972aa2204cae84960bbd78af0988edb3b7be3f0f791d0ea08442d3ca47f858088e2247f168e0a3a2b5f51fedc3e8991a0117ae9c3fe799baed56f4747233d41b75abe1379e1bebe681cf51f4107000060d08bd0f91fbd37ce7310b5db4b4780e5b48ea02baf293ae55d47d0ad331234422f7ddcbf892056f404ddf895eb5af932af44cc7df97964f771d47f041d000080c1e83774f259ff46aa75b4eb22a16124e87c34ab75dc22288af6f957aa9bf85879e5fa3641e728eaffa157aea3fe8557b03d9b27c4f6f3e859636bc7ac7cc9bbbd120e63d7eb47afffbadec7a7eb5f23fc010000fe2059d0f9c8498cba6441134449fa3cfda3814574397dc76d45895c7b9c68d8ea76cebd88f294e922cc9228f462225ef7e5aafea636f8bec66b191d81aad33c5a780691e2af8b7011c1d1e95fcbacfc71fa4affc43e22bad275b19f88ea42104541db13493276cf0f698b25a4fae5cfdbb7625f578e88d49496c5210000c01f4747e82e8538fc6e04ef1e7c0c224d572508363b4d6053610000800b725941e7b05eeb5d1d1de533b705b93293e81c0000005c942b0b3a00000000580041070000007073107400000000370741070000007073107400000000370741773dd81a040000007691059ddae8b5b7ed86de1c36df73689fb5b889ec44b0fcd4b625c5e6b8c601f2f58915cb2c6d0be26c23f7c806badfb8875dd8ec7962ef663360e31e380ef6050080a3e8085d79e492ecfa5f3a96323d089fd126b436f1d8a88f4904eaa73616f6a2eb7dc749cd36eef5f5bb7e8badf7dbf61cab02ba9e077f89ef384bf62fdb1700000ed217748ec16bbfa34e278bc0c92bc5ded15fe5d1529fff3e0b11323ffaebeb591d1d95cb4e51aa0a57464a9f475024ba96f2b9b23fc3fd8d4d3b513ab37e6da378766d4ad311d495fefd8713d15b7e370ed5b9ab4968177d30045e77ec07ed5ba31a3ff77fb1554e9f963f9e1fc9bed2767f2c9bbf47d96854be8c5bbcaed927bac7ed4b74ed3b19bfa9fda6e90000705bba82ce1f2edf89120d44c9085f7e12284341e71c6b7586a710c4a0164772dfe6109b089377d095e8512245ca6b1c67ec5b71ada2e7708320d2f60be2a1bcaf6c73437cd52a6dabeff97462adb0b9f4cf898ef479debfc7bf0f55661d2df4a75c88bdd23dde7eed3874fb3f69df8c50ee76b6acbc76d6827a56fe6c7e78bc2877f5b8b9e7fbe9ee49fd5d69ff9908dd52fb1c3dfbcec66f66bf593a0000dc985ad0a56fefe1b7639653714ec8746ae29c525ecd76af1581321d978f86d4af3da5de81f3e908442d8ca47f85408de2297f168e0a3a2b9f55bec38e3e46629e46d0799bb4f6d3f69df6cf955144a0aa7658af5c9b32e335b3ff45bb127b04908cf196575e7de708e1b4fcc9fc4874c664b5fdc705dd62fb1ca67d85c9f80dedb7940e0000b7a517a1f33fca6f9cd7206ab71771a0bd089d77aeb6a02baf293ae5dd49d08d5fb9ce05c1b87f2ebf44839448afef3f25e816dab78bc7c3d5a32348b3f227f323d113748bed3f2be8a6ed7374ed3b19bf82c67e3bd30100e05e8c7e43279ff56fb85a47b3e6044d4682ce3bafd6718ae029da27afac94385a79e5da173c91a382ce51d47ff0956b2adb1257224ee59599bea619f6cff7cb89e458e623fe1e4bdf5fbf72957b5a51dfefffac7d33c29785ad5cffa542cd9159f9b3f9e1e90abab5f6eb711141d47ef1e8b3d43eb96ed97769fcc6f69ba50300c08dc982ce2dee392a94054d1025e9b319419a881f0b5d4ecfb9da512cd71ee774b7ba9d73550eaa4e176196c48838c974dd97abfa9bdae0fb1aaf65b2c3132166a417c23338687f5d8490387cd331f72335629be787d465098587171db96ed77f71c872df4afff43df283ff0fff3908943026624fd7be784ff947112bfdefb76feb431f897e3d8bfcba7e6156fe687ed8ed2fe7d8acfc2882727addbe19fbdba7ed3b1a3f499fd96f6e5f0000b82d3a42772944906431754fc4e1d6822e08b6f23e8d7fa5378c5e02000000545c56d039acd78e574747f99a6d3526d13900000080435c59d001000000c002083a000000809b83a003000000b839083a000000809b83a003000000b83908baebc1d625000000b08b2ce8f4c6c29d6d37f4c6a6f99ecec6c063e226aa13c1f253db96149bcbfe539dbee0d30ff67b69db12671bb94736b03d64db3987db0f000000d74447e844b06d8efed138fe323d089fd126b93672fa84ec903f8940fdd4c6c25e74ad1fe7b497d9c6c2be7ed76fb1f57edb9ec747078deb0000007061fa82ce3178edd7dcbb48168193578abda3bfcaa399e43c4b1dc59b1ffdf5f5ac8e6eca6507b195f2665c1929dd3a4eab44a26b299f2b5b0e3fafef1d44e9ccfab58de2d99f29ad88a04ab9f1bab75b6eabb2cfa8fd2abfe6274425000000eca42be8fce1f29d28d140948cf0e52781321474f24ab68d140531a8c591dcb70996e615ad7138bf3ebf52ca33858d1671063d311b228fda7e96f02bdbdc105fb54adbea7b3e9d182d6c2efd73a26efb2ca24c0966e375b1d06bbf40840e0000e086d4826e8bce54e2292322c972fa2254cae84e60bbd78a4099c2c28b91fab5671067c57d9a8e40d4c248fa57089c289ef267e1a8a0b3f259e53bece863a427e8bc4d5afb9587e3cb7d1ff1bebe6844d0010000fc327a113affa3fcc6b90fa2767be908b09cd61174dda860a7bc3b09baf12bd789a08dc8b88998fbf2e3648b36041d0000c02f63f41b3af9ac7fc3d50a813591613212743edad70a0b113c45fbe26fca92385a79e5fa3641e728ea3ff8ca3595ddf4c5115e11f76c16f26eaf94836dac76f6da2fe87a1f9fce3e8db006000080cb91059d8fecc4a85016344194a4cfd33f1a584497d317165614cbb5c7899aad6e273e8a2854992ec22c89422f76e2755faeea6f6a83ef6bbc96d111b23acda385671051feba082b114475ffa260ec8924b1cdf343eab284d4c38bba5cb7ebbf8862b96fdebf95f6bb7244e4a6b42c0e010000e0d2e808dda51041d28de0dd830f231216045b799f864d8501000060379715740eebb5e3d5d151be7a63e659740e000000e01057167400000000b000820e000000e0e620e8000000006e0e820e000000e0e620e8000000006ece15051d5b7700000000ec200b3abdb17067db0dbd796dbea7b331f098b8c9ad29d864735e57af6c707ba86c000000803f868ed0954742c9a904a560ab8f8c92530b469be4dac463ad3e3a11b8189993baf6970d000000f007e90b3ac7e0b56773ef2259041a659b478bc57bd286bda5a08cf7c4e3c7d267b9c71fbbe5d3caa3ad000000007e1d5d41e70f97ef44c90e9e78e0cb4f22ae2716e3ab56117ec529119dfb9bfbfcabe350cf43ae3fe2bf2a0f000000c0afa216743932e644d1b338f83e21bf71b3a25ef3c3dfad085c13e5eb093a87fcb18488481fadf382f2117e6fa7ee49f9753e000000805f4d2f42e7ff28a1116e83a8dd5e76be720de922f2c21f4df8ff7f1a513b041d000000fc3546bfa1f3113b2598eaf4f417a9dbe71d745ea1a63acc83f99d587b3ea360f382d31097083a000000f86b6441e784508e8a659126c26afb6c46d00e083a5d4e2dbe24edf921af6fc3eb559de645e03f49e83ddc3d5af4d9af7cf94b59000000f8f5e808dd15605361000000809d5c4dd001000000c04e1074000000003707410700000070731074000000003707410700000070731074000000003707410700000070732c4127a735d41bf2a6cd80d346c0fe1489b481ef37ed19f7787cfcfbf12927441867c07a1e213db6ebeb599df33a454ebe887d92b36c9b3ace953f6fbfd835a44bfdc74fbc889b2c37e332ebdf18bfc974ec7ba26ee3a8fd2bf94ff1211b4fa772ad39f9f8f7f3b9cddb2f3975246e8cbdcde76db3eadcde6cc77efe25fcc6d8b14c8f7526f20837ff8afa3fdd7cd2cf6a9b9ee6e7caf36b8d8f4e3737162fe658b04f18ff50bf7c6e4e7ce9302fff2cad7d4afbbd065d877fce8cb5f4a5f3fe2dc435e4d0c6f1e3fe4307b5b9ffabe74778b6d69fc55fc91bed7b19b4a07b3887f8f9f1f08bbd1ccc9f3ea77479508bf35dc5411d78e04fd339de4bdaad1db92cd67b062e9c531bef7f84e3c7f46274b6fc4cef78b2fabafbbc7f310cedfe14715339c359ff66f84d9fd5e7608fb2bda3f64ff39f44e667a8efe1846b2b96fcbc56f5c9fc2ee6afd17e6dc369fe192737cc96fe7dba71cbd7fc186ea7a178a1e0cacf22ceb5571cf226ea9cbd27cf6f3d46569b7be316ec5fb6ef297371a71379f5bc48ccecf712bc4d8d536e6e897c01dcb9beffaafeff0cef9affde2ffc65411779977d2f411da19328928fe2b807f9a31a7cbf204a04262df08d43700ba4bb27ab602923e70dd7b23a76ce327ddefd2db076bc1d0e0bae8877e083c5de2e5f8e24937eb58222d3697f7038ea9ae14c67e507a7eafe6fe62d99f56f46edfcd7dabfd18807cf82fd7a88c08a73ca8ad0850779133c16d97ede99956d58c93f64614cbaccf24abaf11cc97397c678fefc6e63325af4ccb44efd4718d57d787e2cd9bebf7e499bfc3589cac6f47f9af91faf6bb44dd49a67f74f45d0a56c3756e9de54ff36966d39fa5a7e93a0db98d676b92ef7d56f188a74790ee4f3fa98cefa3f6d5fd7feae1df1ded03e9747d6d026ff0c57af2a5fda567ee13837fe4d7ef7fff299e897afe9ceffd9f84d485ff48b3996fa2ecf872f77b347eaf3ba7f7efc69fb5e8246d089619d212c359f1c76fab75ec49b3cde409b511a43ca4379c4c10d059d1bd43828879da710fbd60ee8acfc170b3a65df40bf7c6f5fedac47fdeff66f11a3fcb5f6abb4a3f61bf0783cdc62ef1612d796f0ea4f470b5c9a5cf7e5c70756b7d72373561c47d517cf4afe01d2e798d7a317d419c3393f58a454be343e799c8cf1d1af5d7bf599754ddab7876e5f3c07e7c742fb56d62f3d667e2ed4658ee67ca4d7bfe070e3f5183dd4f749bee20b98d527774d9cb23c5b7e6eba3ea439fae99c65f1bc4bffdc1c0e9fc3bcdfe66398ebebce3c32ebffa87d23fbfb6727a4cbcf3afcf8bbff4b1b57bf94867b95bda47c2560ce8e7f18d7ed0bdf43faaa6c312b3fd19d1fc3f19be39f6da92fb5c1d7af9ea3663ec91c54e913feba7d2f412de83e3ea3aa95ce54510e31b037587c681ffae195ff1b0e3ae7f19fdd62ec062c19ad1980559a8967200b62352196717da9a3330d67caefb4bfb49543db7701eb1bb2d9be95fe4d9087a25e48f7b4dfca7f962046dc98b807f3d3cddd0f3746d67d897a41c8c875536c9694f965f16aed3f141ec53333c93f99f3bd454ae71b3ebf91c311baa27d555f166ca919d57d9889fdcab1d8d073ba99b3569983399f30fb67e5abca5faabfd74f295f8f496630bf16fad230cb336adfc8feba5c57469a538d4dbab839396ad7ac7ef7ffb9fda58ecdb6fee70d693d5c283fd19d1fc5b82506eb4b85e56fcb3e5502ae375626d8f712587f14d1a331be137ff9215b34681e34192ce3fe2556279ab46934c90cfc8ffa5dbb6a556f72a07c4fa7fd22c88a09ddb1e9129dbcbbfad7c5fee6b6defe7ddffcf622732c3c8cd502d120c2c368871b9f3511d2c9bfcc6411d4cce65a275d8fc9f0f98dac8c4b7741346c662da833ccf2cfb262bf49fbf3da95d2ade778568fa36bbf3a5f55fe52fd9db5653ad70ef6a56196a7d7be99fd75b9ae8ca382aebbeecdea77ff5fb27fc27d99f491c4d4e685f213e6fcd8b35674980b3a47eed3de351afb5e82a3822e18c0394d6584b59067b876383a2774065acadc42be0fd75ed73e63906d42c4adf8d17b35c1d6ca3ffeca5526a5ae5fecdd2e568baf9c9a093eef5fba6f5a7ee7e1596bbfa397df73f0955ac6cdaf94b7a847aebb3957bc12e8b4c38d4f3bae3bf27790f9a37fdbe7ff6861477eb167316ffcf3b5d958e6a3b4273d6fd61f458c9e5fe1b0a073f8675adb47ae1575aed12b3f707c7eccecd7ac49d5fad53c2fd6732c73a2b269cdd07ea97e7903e0c647dfa7f3f93fc8717668ca1938c1d0ffde7c93be3a9be6fe3ffcfdf5fc9832ebffa07d43fbeb72d5f3d98cc900ff7c687bfaf2b72f3c67c75ff2eb578e12c1d7ebc3acfc446f7e8cc76f4efdca35fc5157fd1c0521e77f6bdb19a71e7fddbe976055d079f1e217523568f26039836df705d190ef7369d6a22b06dde3c812ba0d195d7f7c0d9ad2b4739b220b862e37524cb0a5f2dd24f2e9adc399b6df117e1f12d2ec07aa5f7ec21cab95fe79e6e5370f5e91366bff38ff4afd33fcfcf265e87aa45cf7598d5ff3a35bc346bbf24fa9e74fbbd8ccf01156557f29a01e7e51b2ca5f797ec32beb44b5383accf95b3cc765ff82fded3960312f5f38373f66f6ebad5fdb9c8a73426c97ee8bf62ced17c9ed4fedaed1fd70f7a432646ed54e353a489d2eff0ff7d8e597cf59393ffe710eb058c374f97eec6299d51ad5e348ffebf6d9f65779a5bc687be9771a97b29c1ed5fc1401f0d2f17fb6eb43f10cf5cb5f9b1f8ff1f80d08cf96f457ad714dfb22be6fbade55feae7d2fc39e081d00007c0f67ff4a1fe0104e4c31ef6e0a820e00e01ae828d72db74d80db5246c8db083ddc00041d000000c0cd41d001c05be9fc7e73e3c8ef750000a00041070000007073107400000000370741070000007073ae28e8fce6a6f2bb9bbc87110000000074c9824e6de497a8ff6c5e6ffe97ef39b45f4ddce4cf146c2eedcbd5cb5e38000000006be8089d08b64d44c9aec9a5602bd365df1abd13ff2a8f7ffd111db263b525e862644eeada5f36000000c01fa42fe81c83d79ecdbd8b641168943d3afa276db8590aca78cf57389a267d967bfcb1393e8d2d11000000e097d31574fe70e84e944cc498bc16adaf4ff0e52711d7138bf155ab08bff2a05dfbfee63effea38d4e30f217ec47f551e000000805f452de87264ac7bb0b6fcc6cd8a7acd0fbfb522704d94af27e81cf2c71222227db4ce0bca47f8bd9dba8773e8000000e0cfd18bd0f93f4a6884db206ab7979daf5c43ba88bcf04713feff9f46d40e41070000007f8dd16fe87cc44e09a63a3dfd45eaf679079d57a8a90eff8713f5ab5227d69ecf28d8bce034c425820e000000fe1a59d0392194a36259a489b0da3e9b11b403824e97538b2f497b7ec8ebdbf07a55a77911f84f127a0f778f167df62b5ffe52160000007e3d3a427705d85418000000602757137400000000b013041d000000c0cd41d001000000dc1c041d000000c0cd41d001000000dc1c041d000000c0cd41d001000000dc9c2ce8f4c6c209bf275c956101d91cd83cad41d5313fcd216e146c6c5cdc2dff9720c79a6de3d09e8671f7fefff6f1030000f87674844e8eddd2e241ce733d7212c48cf608b11e278e16bb2bb2a9b213d2cd291937c16f0c6d5c0700008037321374e9c406499388514a97284b13695b8cc075059d3f785f47a62a4137283fb5ef4bce7a8df7f808a3ba27084495f619ca5b1397c2a38c9eb9b63d8b33671fff7eaaf42f979eea5f695fb269c162ff03fdfead8c9fbe26797d3b751b3fa4bfb17c199f4f759a873f962da56d14d1c569fbcfd90f0000e0cf520bbae44c03e501f9925e3b68cb31cb7db6c31ea58b18710e5a89232f9eb4a089f4caf7ed5765487e7d9f3ff0ff237e7e84736a47edac91f29e29bf200254d9c897afec1504ea678eb6cdda17f248846e1c95ecf57fd63fc9371d3f2fba5cf94ec83fa49dae0ffe5f9fee84a1ea9f08c0a23cc74a846ed8feb3f6030000f88b8c2274c1a16e4e764910387a0e3b61a65b6575c44daffc61fbacb23aedb709e2c24e7348f9c6f9b3223a76d9efa8a05be8df52fd239bb83ab6089d44d05e28e85e653f000080bfc850d055d7561daae9b01566ba555647dcf4ca1fb66f41f08c29a3450daf12249d3e6bccfe2ff46fa9feae4d5cff251aa922944d790e041d0000c00f308dd0fdb34561b4237e7c38072c511ac3a19a0e5b61a78b60da5ea71d7de53a72f8c52bbd43af5cab57983e82b9d967e595e154901c15748e59ff74beeef8f544926fd733bf7295fcbaef09dd8687ff1d5efb071e4bed178ed8cfcd1bffd7d1ae5e5b589e4d070000b82059d039e758fe7e2e5038cc28607c9a88afe28f0a5c9acab7911ce32cbd2a5f7e742f7f1421fff702679c5f9c7dbae69dbeeacfd607557e6cbf252cfa943fdaff470488fe4d5d952ec22cf56da57d22c0d2b54c8e5a2dd86fd6bf03e3a70594eec3d7d3f5cd7f2e4598ff439a945feac8692bed3f67bfb21e5daee66c3a0000c005d111babf8608927d82ee5efcf6fe01000040e4af093a1d05932853f7377137e5b7f70f0000000cfe72840e000000e05780a003000000b839083a000000809b83a003000000b839083a000000809b83a003000000b83996a0db3671ad76eeff8f787a83b12d86def8b5d908d87f96fbb66bec8f06000000f0227a82ce125cfee8abe7471671fea40027d67a473389f8d3f7277ae503000000c00196055de78c51117949c46d824ece110d62aebe3fdd87a0030000007811ab82ae2bc2e4ecce783d08ba0f7f5ee848b021e8000000005ec8ab059dfc3eeef9f9e10f59ef8936041d000000c00b79cf2b57415ebb3efffd50f7261074000000002fe49d7f14e1a377c6efe81074000000002f648fa0dbb76d895c8fdb9454913d041d000000c00bd927e85e03820e000000e085f4045d88b6d51b0b9f858d85010000005e8e25e800000000e04620e8000000006e0e820e000000e0e620e800000000eecc7ffcfb7f03e702469ba99e0b490000000049454e44ae426082, '2026-03-01 16:33:24');

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

--
-- Dumping data for table `loan`
--

INSERT INTO `loan` (`loanId`, `loanType`, `amount`, `duration`, `interest_rate`, `remaining_principal`, `status`, `createdAt`, `id_user`) VALUES
(1, 'HOUSING', 100000.00, 74, 6.75, 95589.77, 'ACTIVE', '2026-03-02 09:23:39', 65),
(2, 'CAR', 50000.00, 60, 10.00, 50000.00, 'ACTIVE', '2026-03-03 08:33:28', 63);

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

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `type`, `message`, `is_read`, `created_at`) VALUES
(1, 22, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 0, '2026-02-23 13:32:31'),
(2, 22, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 0, '2026-02-23 13:32:33'),
(3, 30, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 1, '2026-02-23 14:19:59'),
(4, 30, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 1, '2026-02-24 01:35:22'),
(5, 30, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 1, '2026-02-24 09:10:02'),
(6, 30, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 1, '2026-02-24 09:10:26'),
(7, 54, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 1, '2026-02-24 09:22:49'),
(8, 54, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 1, '2026-02-24 09:36:29'),
(9, 55, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 0, '2026-02-24 09:53:15'),
(10, 32, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 0, '2026-02-27 23:41:56'),
(11, 35, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 0, '2026-02-28 00:14:29'),
(12, 56, 'ADMIN', 'Votre compte a ete valide par l\'administration.', 1, '2026-02-28 00:50:16'),
(13, 56, 'SYSTEM', 'Votre profil a ete mis a jour avec succes.', 1, '2026-02-28 01:02:39'),
(14, 56, 'ADMIN', 'coucou youssef', 1, '2026-02-28 01:44:34'),
(15, 62, 'KYC', 'Votre KYC a ete valide. Votre wallet FinTrust est maintenant cree et disponible.', 0, '2026-02-28 17:53:43'),
(16, 63, 'ADMIN', 'Votre compte est remis en attente de verification.', 0, '2026-02-28 18:10:30'),
(17, 63, 'ADMIN', 'Votre compte a ete valide par l\'administration.', 0, '2026-02-28 18:10:39'),
(18, 63, 'KYC', 'Votre KYC a ete valide. Votre wallet FinTrust est maintenant cree et disponible.', 0, '2026-02-28 18:11:55'),
(19, 64, 'ADMIN', 'Votre compte a ete valide par l\'administration.', 0, '2026-03-01 15:37:23'),
(20, 66, 'ADMIN', 'testmssage', 0, '2026-03-02 11:34:12'),
(21, 65, 'ADMIN', 'Votre compte a ete mis a jour par l\'administration.', 1, '2026-03-02 12:40:31'),
(22, 65, 'SYSTEM', 'Votre profil a ete mis a jour avec succes.', 1, '2026-03-02 22:45:55'),
(23, 66, 'ADMIN', 'Votre compte est remis en attente de verification.', 0, '2026-03-02 22:53:56'),
(24, 63, 'KYC', 'Votre KYC a ete valide. Votre wallet FinTrust est maintenant cree et disponible.', 0, '2026-03-03 08:42:09'),
(25, 30, 'ADMIN', 'cooucouuu', 0, '2026-03-03 09:02:54');

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

--
-- Dumping data for table `otp_audit`
--

INSERT INTO `otp_audit` (`id`, `user_id`, `email`, `channel`, `event_type`, `request_id`, `success`, `reason`, `validation_seconds`, `created_at`) VALUES
(12, 54, 'maryemsaid.42@gmail.com', 'EMAIL', 'REQUEST', '5c806f3c-e618-4d73-b3e0-336c38e0258a', 1, 'sent', NULL, '2026-02-24 09:21:54'),
(13, 54, 'maryemsaid.42@gmail.com', 'EMAIL', 'VALIDATE', '26972699-e9ac-4ebb-98e8-fec4cf625d7c', 1, 'ok', NULL, '2026-02-24 09:22:18'),
(14, 55, 'khaled.guedria@esprit.tn', 'EMAIL', 'REQUEST', '57711f25-b5d0-4083-a1f3-07d31bb4fa9d', 1, 'sent', NULL, '2026-02-24 09:52:33'),
(15, 55, 'khaled.guedria@esprit.tn', 'EMAIL', 'VALIDATE', '20deedcc-fadc-45df-bbd3-3d7cb07c0275', 1, 'ok', NULL, '2026-02-24 09:53:00'),
(16, 65, 'mohamedhedi322@gmail.com', 'EMAIL', 'REQUEST', '4182aeb6-3036-4d7e-8952-8e0db9f5c899', 1, 'sent', NULL, '2026-03-02 11:06:18'),
(17, 65, 'mohamedhedi322@gmail.com', 'EMAIL', 'REQUEST', '7cb44808-d0cc-42e0-8d21-ffdac6246a15', 1, 'sent', NULL, '2026-03-02 11:06:37'),
(18, 65, 'mohamedhedi322@gmail.com', 'EMAIL', 'VALIDATE', '9c30fd13-e8cb-4459-9a01-75dcecd98a2d', 1, 'ok', NULL, '2026-03-02 11:09:36'),
(19, 65, 'mohamedhedi322@gmail.com', 'EMAIL', 'REQUEST', '225c5b0a-0328-4391-adc9-68aef0c57ea3', 1, 'sent', NULL, '2026-03-02 22:45:07'),
(20, 65, 'mohamedhedi322@gmail.com', 'EMAIL', 'VALIDATE', '10552b30-3390-41ca-8ae7-8acce0369f32', 1, 'ok', NULL, '2026-03-02 22:45:22');

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

--
-- Dumping data for table `password_reset`
--

INSERT INTO `password_reset` (`id`, `user_id`, `code_hash`, `expires_at`, `used_at`, `created_at`, `attempts`) VALUES
(9, 54, '$2a$10$7wLxrgfFkBMwYC14pSKHDOGBA.tfsXY2aBCleJmIeVEubtmPfqbOO', '2026-02-24 09:31:50', '2026-02-24 09:22:18', '2026-02-24 09:21:50', 0),
(10, 55, '$2a$10$sT3ZmFki7ZQEwiPgBNWiOuYnFLO.3CgLxo3FrxUXmvi7yIkaaBiTq', '2026-02-24 10:02:30', '2026-02-24 09:53:00', '2026-02-24 09:52:30', 0),
(12, 65, '$2a$10$QKbk.vtO.KEAmv9WtBPczueesZZZObTwOceS.IKqNOL1mfkjVJk2.', '2026-03-02 11:16:15', '2026-03-02 11:06:35', '2026-03-02 11:06:15', 0),
(13, 65, '$2a$10$zppfmY81yvnD544TsafP.eCi6xx6Ml8eMKztFGaufE00Lq7CPmG.G', '2026-03-02 11:16:35', '2026-03-02 11:09:36', '2026-03-02 11:06:35', 0),
(14, 65, '$2a$10$RS6f1IgnC.2apRlCput1q.JyFUMFtUhB8rj7HsJI6noopEQV4rjui', '2026-03-02 22:55:05', '2026-03-02 22:45:22', '2026-03-02 22:45:05', 0);

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

--
-- Dumping data for table `product`
--

INSERT INTO `product` (`productId`, `category`, `price`, `description`, `createdAt`) VALUES
(1, 'COMPTE_EPARGNE', 200, 'test1', '2026-03-01'),
(2, 'COMPTE_EPARGNE', 134, 'test2', '2026-03-01'),
(3, 'CARTE_VIRTUELLE', 150, 'test3', '2026-03-01'),
(4, 'ASSURANCE_VIE', 500, 'test4', '2026-03-01'),
(5, 'PLACEMENT_INVESTISSEMENT', 1000, 'test5', '2026-03-01'),
(6, 'CARTE_CREDIT', 1005, 'test6', '2026-03-01'),
(7, 'CARTE_VIRTUELLE', 145, 'test7', '2026-03-01'),
(8, 'CARTE_DEBIT', 100, 'Produit test pour subscription', '2026-03-03'),
(10, 'CARTE_DEBIT', 100, 'Produit test pour subscription', '2026-03-03'),
(12, 'CARTE_DEBIT', 100, 'Produit test pour subscription', '2026-03-03');

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

--
-- Dumping data for table `productsubscription`
--

INSERT INTO `productsubscription` (`subscriptionId`, `client`, `product`, `type`, `subscriptionDate`, `expirationDate`, `status`) VALUES
(16, 65, 4, 'MONTHLY', '2026-02-23', '2026-04-01', 'CLOSED'),
(17, 65, 2, 'MONTHLY', '2026-03-01', '2026-04-01', 'CLOSED'),
(18, 65, 4, 'MONTHLY', '2026-02-26', '2026-04-01', 'ACTIVE'),
(19, 65, 5, 'MONTHLY', '2026-03-01', '2026-04-01', 'ACTIVE'),
(20, 65, 7, 'MONTHLY', '2026-03-01', '2026-04-01', 'ACTIVE'),
(21, 65, 7, 'ANNUAL', '2026-02-25', '2027-03-01', 'ACTIVE'),
(22, 65, 2, 'ONE_TIME', '2026-02-18', '2026-03-01', 'DRAFT'),
(23, 65, 1, 'ONE_TIME', '2026-03-01', '2026-03-01', 'ACTIVE'),
(24, 65, 3, 'ANNUAL', '2026-03-01', '2027-03-01', 'ACTIVE'),
(25, 65, 3, 'TRANSACTION', '2026-02-18', '2026-03-02', 'ACTIVE'),
(26, 65, 5, 'TRANSACTION', '2026-03-01', '2026-03-02', 'ACTIVE'),
(27, 65, 6, 'ANNUAL', '2026-03-02', '2027-03-02', 'ACTIVE'),
(31, 65, 5, 'MONTHLY', '2026-03-03', '2026-04-03', 'ACTIVE');

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

--
-- Dumping data for table `publication`
--

INSERT INTO `publication` (`id_publication`, `titre`, `contenu`, `categorie`, `statut`, `est_visible`, `date_publication`) VALUES
(1, 'Lancement de la plateforme', 'Nous sommes heureux d’annoncer le lancement officiel de la plateforme.', 'Actualité', 'PUBLIÉ', 1, '2026-02-09 13:34:11'),
(2, 'test2', 'test2_2', 'Investissement', 'Publié', 1, '2026-03-14 00:00:00'),
(4, 'Maintenance programmée', 'Une maintenance est prévue ce week-end.', 'Information', 'BROUILLON', 0, '2026-02-15 16:04:59'),
(5, 'Lancement de la plateforme', 'Nous sommes heureux d’annoncer le lancement officiel de la plateforme.', 'Actualité', 'PUBLIÉ', 1, '2026-02-15 17:25:51'),
(6, 'Maintenance programmée', 'Une maintenance est prévue ce week-end.', 'Information', 'BROUILLON', 0, '2026-02-15 17:25:51'),
(8, 'Maintenance programmée', 'Une maintenance est prévue ce week-end.', 'Information', 'BROUILLON', 0, '2026-02-15 19:53:17'),
(9, 'Lancement de la plateforme', 'Nous sommes heureux d’annoncer le lancement officiel de la plateforme.', 'Actualité', 'PUBLIÉ', 1, '2026-02-15 19:54:16'),
(12, 'Maintenance programmée', 'Une maintenance est prévue ce week-end.', 'Information', 'BROUILLON', 0, '2026-02-15 19:57:55'),
(14, 'Maintenance programmée', 'Une maintenance est prévue ce week-end.', 'Information', 'BROUILLON', 0, '2026-02-16 17:37:00'),
(16, 'Maintenance programmée', 'Une maintenance est prévue ce week-end.', 'Information', 'BROUILLON', 0, '2026-02-16 17:37:05'),
(36, 'Trading is SCAM', 'Here my story about trading after a year trqding SP500.', 'Trading et Investissement', 'Brouillon', 1, '2026-02-17 00:00:00'),
(37, 'Prets Bancaires', 'hzhoioijvpipizjpzjv', 'Finance', 'Publié', 1, '2026-02-27 00:00:00'),
(38, 'Assurez vous!', 'nouvelle actualités!', 'Assurance', 'Publié', 0, '2026-02-25 00:00:00'),
(40, 'Trading', 'gùoskgùkùosfùsfkm', 'Investissement', 'Brouillon', 1, '2026-04-03 00:00:00'),
(41, 'Epargne', 'jfljmzzokpzvpo', 'Investissement', 'Publié', 1, '2026-03-11 00:00:00');

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

--
-- Dumping data for table `repayment`
--

INSERT INTO `repayment` (`repayId`, `loanId`, `month`, `startingBalance`, `monthlyPayment`, `capitalPart`, `interestPart`, `remainingBalance`, `status`) VALUES
(1, 1, 1, 100000.00, 1655.80, 1093.30, 562.50, 98906.70, 'PAID'),
(2, 1, 2, 98906.70, 1655.80, 1099.45, 556.35, 97807.25, 'PAID'),
(3, 1, 3, 97807.25, 1655.80, 1105.63, 550.17, 96701.62, 'PAID'),
(4, 1, 4, 96701.62, 1655.80, 1111.85, 543.95, 95589.76, 'PAID'),
(5, 1, 5, 95589.76, 1655.80, 1118.11, 537.69, 94471.66, 'UNPAID'),
(6, 1, 6, 94471.66, 1655.80, 1124.40, 531.40, 93347.26, 'UNPAID'),
(7, 1, 7, 93347.26, 1655.80, 1130.72, 525.08, 92216.54, 'UNPAID'),
(8, 1, 8, 92216.54, 1655.80, 1137.08, 518.72, 91079.46, 'UNPAID'),
(9, 1, 9, 91079.46, 1655.80, 1143.48, 512.32, 89935.98, 'UNPAID'),
(10, 1, 10, 89935.98, 1655.80, 1149.91, 505.89, 88786.07, 'UNPAID'),
(11, 1, 11, 88786.07, 1655.80, 1156.38, 499.42, 87629.69, 'UNPAID'),
(12, 1, 12, 87629.69, 1655.80, 1162.88, 492.92, 86466.81, 'UNPAID'),
(13, 1, 13, 86466.81, 1655.80, 1169.42, 486.38, 85297.38, 'UNPAID'),
(14, 1, 14, 85297.38, 1655.80, 1176.00, 479.80, 84121.38, 'UNPAID'),
(15, 1, 15, 84121.38, 1655.80, 1182.62, 473.18, 82938.76, 'UNPAID'),
(16, 1, 16, 82938.76, 1655.80, 1189.27, 466.53, 81749.49, 'UNPAID'),
(17, 1, 17, 81749.49, 1655.80, 1195.96, 459.84, 80553.53, 'UNPAID'),
(18, 1, 18, 80553.53, 1655.80, 1202.69, 453.11, 79350.85, 'UNPAID'),
(19, 1, 19, 79350.85, 1655.80, 1209.45, 446.35, 78141.40, 'UNPAID'),
(20, 1, 20, 78141.40, 1655.80, 1216.25, 439.55, 76925.14, 'UNPAID'),
(21, 1, 21, 76925.14, 1655.80, 1223.10, 432.70, 75702.05, 'UNPAID'),
(22, 1, 22, 75702.05, 1655.80, 1229.98, 425.82, 74472.07, 'UNPAID'),
(23, 1, 23, 74472.07, 1655.80, 1236.89, 418.91, 73235.18, 'UNPAID'),
(24, 1, 24, 73235.18, 1655.80, 1243.85, 411.95, 71991.32, 'UNPAID'),
(25, 1, 25, 71991.32, 1655.80, 1250.85, 404.95, 70740.48, 'UNPAID'),
(26, 1, 26, 70740.48, 1655.80, 1257.88, 397.92, 69482.59, 'UNPAID'),
(27, 1, 27, 69482.59, 1655.80, 1264.96, 390.84, 68217.63, 'UNPAID'),
(28, 1, 28, 68217.63, 1655.80, 1272.08, 383.72, 66945.55, 'UNPAID'),
(29, 1, 29, 66945.55, 1655.80, 1279.23, 376.57, 65666.32, 'UNPAID'),
(30, 1, 30, 65666.32, 1655.80, 1286.43, 369.37, 64379.90, 'UNPAID'),
(31, 1, 31, 64379.90, 1655.80, 1293.66, 362.14, 63086.23, 'UNPAID'),
(32, 1, 32, 63086.23, 1655.80, 1300.94, 354.86, 61785.29, 'UNPAID'),
(33, 1, 33, 61785.29, 1655.80, 1308.26, 347.54, 60477.04, 'UNPAID'),
(34, 1, 34, 60477.04, 1655.80, 1315.62, 340.18, 59161.42, 'UNPAID'),
(35, 1, 35, 59161.42, 1655.80, 1323.02, 332.78, 57838.40, 'UNPAID'),
(36, 1, 36, 57838.40, 1655.80, 1330.46, 325.34, 56507.94, 'UNPAID'),
(37, 1, 37, 56507.94, 1655.80, 1337.94, 317.86, 55170.00, 'UNPAID'),
(38, 1, 38, 55170.00, 1655.80, 1345.47, 310.33, 53824.53, 'UNPAID'),
(39, 1, 39, 53824.53, 1655.80, 1353.04, 302.76, 52471.50, 'UNPAID'),
(40, 1, 40, 52471.50, 1655.80, 1360.65, 295.15, 51110.85, 'UNPAID'),
(41, 1, 41, 51110.85, 1655.80, 1368.30, 287.50, 49742.55, 'UNPAID'),
(42, 1, 42, 49742.55, 1655.80, 1376.00, 279.80, 48366.55, 'UNPAID'),
(43, 1, 43, 48366.55, 1655.80, 1383.74, 272.06, 46982.81, 'UNPAID'),
(44, 1, 44, 46982.81, 1655.80, 1391.52, 264.28, 45591.29, 'UNPAID'),
(45, 1, 45, 45591.29, 1655.80, 1399.35, 256.45, 44191.94, 'UNPAID'),
(46, 1, 46, 44191.94, 1655.80, 1407.22, 248.58, 42784.72, 'UNPAID'),
(47, 1, 47, 42784.72, 1655.80, 1415.14, 240.66, 41369.58, 'UNPAID'),
(48, 1, 48, 41369.58, 1655.80, 1423.10, 232.70, 39946.49, 'UNPAID'),
(49, 1, 49, 39946.49, 1655.80, 1431.10, 224.70, 38515.39, 'UNPAID'),
(50, 1, 50, 38515.39, 1655.80, 1439.15, 216.65, 37076.24, 'UNPAID'),
(51, 1, 51, 37076.24, 1655.80, 1447.25, 208.55, 35628.99, 'UNPAID'),
(52, 1, 52, 35628.99, 1655.80, 1455.39, 200.41, 34173.60, 'UNPAID'),
(53, 1, 53, 34173.60, 1655.80, 1463.57, 192.23, 32710.03, 'UNPAID'),
(54, 1, 54, 32710.03, 1655.80, 1471.81, 183.99, 31238.22, 'UNPAID'),
(55, 1, 55, 31238.22, 1655.80, 1480.08, 175.72, 29758.14, 'UNPAID'),
(56, 1, 56, 29758.14, 1655.80, 1488.41, 167.39, 28269.73, 'UNPAID'),
(57, 1, 57, 28269.73, 1655.80, 1496.78, 159.02, 26772.95, 'UNPAID'),
(58, 1, 58, 26772.95, 1655.80, 1505.20, 150.60, 25267.74, 'UNPAID'),
(59, 1, 59, 25267.74, 1655.80, 1513.67, 142.13, 23754.08, 'UNPAID'),
(60, 1, 60, 23754.08, 1655.80, 1522.18, 133.62, 22231.89, 'UNPAID'),
(61, 1, 61, 22231.89, 1655.80, 1530.75, 125.05, 20701.15, 'UNPAID'),
(62, 1, 62, 20701.15, 1655.80, 1539.36, 116.44, 19161.79, 'UNPAID'),
(63, 1, 63, 19161.79, 1655.80, 1548.01, 107.79, 17613.78, 'UNPAID'),
(64, 1, 64, 17613.78, 1655.80, 1556.72, 99.08, 16057.05, 'UNPAID'),
(65, 1, 65, 16057.05, 1655.80, 1565.48, 90.32, 14491.58, 'UNPAID'),
(66, 1, 66, 14491.58, 1655.80, 1574.28, 81.52, 12917.29, 'UNPAID'),
(67, 1, 67, 12917.29, 1655.80, 1583.14, 72.66, 11334.15, 'UNPAID'),
(68, 1, 68, 11334.15, 1655.80, 1592.05, 63.75, 9742.11, 'UNPAID'),
(69, 1, 69, 9742.11, 1655.80, 1601.00, 54.80, 8141.10, 'UNPAID'),
(70, 1, 70, 8141.10, 1655.80, 1610.01, 45.79, 6531.10, 'UNPAID'),
(71, 1, 71, 6531.10, 1655.80, 1619.06, 36.74, 4912.04, 'UNPAID'),
(72, 1, 72, 4912.04, 1655.80, 1628.17, 27.63, 3283.87, 'UNPAID'),
(73, 1, 73, 3283.87, 1655.80, 1637.33, 18.47, 1646.54, 'UNPAID'),
(74, 1, 74, 1646.54, 1655.80, 1646.54, 9.26, 0.00, 'UNPAID'),
(76, 2, 1, 50000.00, 1062.35, 645.69, 416.67, 49354.31, 'UNPAID'),
(77, 2, 2, 49354.31, 1062.35, 651.07, 411.29, 48703.25, 'UNPAID'),
(78, 2, 3, 48703.25, 1062.35, 656.49, 405.86, 48046.76, 'UNPAID'),
(79, 2, 4, 48046.76, 1062.35, 661.96, 400.39, 47384.79, 'UNPAID'),
(80, 2, 5, 47384.79, 1062.35, 667.48, 394.87, 46717.31, 'UNPAID'),
(81, 2, 6, 46717.31, 1062.35, 673.04, 389.31, 46044.27, 'UNPAID'),
(82, 2, 7, 46044.27, 1062.35, 678.65, 383.70, 45365.62, 'UNPAID'),
(83, 2, 8, 45365.62, 1062.35, 684.31, 378.05, 44681.32, 'UNPAID'),
(84, 2, 9, 44681.32, 1062.35, 690.01, 372.34, 43991.31, 'UNPAID'),
(85, 2, 10, 43991.31, 1062.35, 695.76, 366.59, 43295.55, 'UNPAID'),
(86, 2, 11, 43295.55, 1062.35, 701.56, 360.80, 42594.00, 'UNPAID'),
(87, 2, 12, 42594.00, 1062.35, 707.40, 354.95, 41886.59, 'UNPAID'),
(88, 2, 13, 41886.59, 1062.35, 713.30, 349.05, 41173.30, 'UNPAID'),
(89, 2, 14, 41173.30, 1062.35, 719.24, 343.11, 40454.06, 'UNPAID'),
(90, 2, 15, 40454.06, 1062.35, 725.24, 337.12, 39728.82, 'UNPAID'),
(91, 2, 16, 39728.82, 1062.35, 731.28, 331.07, 38997.54, 'UNPAID'),
(92, 2, 17, 38997.54, 1062.35, 737.37, 324.98, 38260.17, 'UNPAID'),
(93, 2, 18, 38260.17, 1062.35, 743.52, 318.83, 37516.65, 'UNPAID'),
(94, 2, 19, 37516.65, 1062.35, 749.71, 312.64, 36766.94, 'UNPAID'),
(95, 2, 20, 36766.94, 1062.35, 755.96, 306.39, 36010.98, 'UNPAID'),
(96, 2, 21, 36010.98, 1062.35, 762.26, 300.09, 35248.72, 'UNPAID'),
(97, 2, 22, 35248.72, 1062.35, 768.61, 293.74, 34480.10, 'UNPAID'),
(98, 2, 23, 34480.10, 1062.35, 775.02, 287.33, 33705.08, 'UNPAID'),
(99, 2, 24, 33705.08, 1062.35, 781.48, 280.88, 32923.61, 'UNPAID'),
(100, 2, 25, 32923.61, 1062.35, 787.99, 274.36, 32135.62, 'UNPAID'),
(101, 2, 26, 32135.62, 1062.35, 794.56, 267.80, 31341.06, 'UNPAID'),
(102, 2, 27, 31341.06, 1062.35, 801.18, 261.18, 30539.89, 'UNPAID'),
(103, 2, 28, 30539.89, 1062.35, 807.85, 254.50, 29732.03, 'UNPAID'),
(104, 2, 29, 29732.03, 1062.35, 814.59, 247.77, 28917.45, 'UNPAID'),
(105, 2, 30, 28917.45, 1062.35, 821.37, 240.98, 28096.08, 'UNPAID'),
(106, 2, 31, 28096.08, 1062.35, 828.22, 234.13, 27267.86, 'UNPAID'),
(107, 2, 32, 27267.86, 1062.35, 835.12, 227.23, 26432.74, 'UNPAID'),
(108, 2, 33, 26432.74, 1062.35, 842.08, 220.27, 25590.66, 'UNPAID'),
(109, 2, 34, 25590.66, 1062.35, 849.10, 213.26, 24741.56, 'UNPAID'),
(110, 2, 35, 24741.56, 1062.35, 856.17, 206.18, 23885.39, 'UNPAID'),
(111, 2, 36, 23885.39, 1062.35, 863.31, 199.04, 23022.08, 'UNPAID'),
(112, 2, 37, 23022.08, 1062.35, 870.50, 191.85, 22151.58, 'UNPAID'),
(113, 2, 38, 22151.58, 1062.35, 877.76, 184.60, 21273.82, 'UNPAID'),
(114, 2, 39, 21273.82, 1062.35, 885.07, 177.28, 20388.75, 'UNPAID'),
(115, 2, 40, 20388.75, 1062.35, 892.45, 169.91, 19496.31, 'UNPAID'),
(116, 2, 41, 19496.31, 1062.35, 899.88, 162.47, 18596.42, 'UNPAID'),
(117, 2, 42, 18596.42, 1062.35, 907.38, 154.97, 17689.04, 'UNPAID'),
(118, 2, 43, 17689.04, 1062.35, 914.94, 147.41, 16774.10, 'UNPAID'),
(119, 2, 44, 16774.10, 1062.35, 922.57, 139.78, 15851.53, 'UNPAID'),
(120, 2, 45, 15851.53, 1062.35, 930.26, 132.10, 14921.27, 'UNPAID'),
(121, 2, 46, 14921.27, 1062.35, 938.01, 124.34, 13983.27, 'UNPAID'),
(122, 2, 47, 13983.27, 1062.35, 945.83, 116.53, 13037.44, 'UNPAID'),
(123, 2, 48, 13037.44, 1062.35, 953.71, 108.65, 12083.73, 'UNPAID'),
(124, 2, 49, 12083.73, 1062.35, 961.65, 100.70, 11122.08, 'UNPAID'),
(125, 2, 50, 11122.08, 1062.35, 969.67, 92.68, 10152.41, 'UNPAID'),
(126, 2, 51, 10152.41, 1062.35, 977.75, 84.60, 9174.66, 'UNPAID'),
(127, 2, 52, 9174.66, 1062.35, 985.90, 76.46, 8188.77, 'UNPAID'),
(128, 2, 53, 8188.77, 1062.35, 994.11, 68.24, 7194.65, 'UNPAID'),
(129, 2, 54, 7194.65, 1062.35, 1002.40, 59.96, 6192.26, 'UNPAID'),
(130, 2, 55, 6192.26, 1062.35, 1010.75, 51.60, 5181.51, 'UNPAID'),
(131, 2, 56, 5181.51, 1062.35, 1019.17, 43.18, 4162.33, 'UNPAID'),
(132, 2, 57, 4162.33, 1062.35, 1027.67, 34.69, 3134.67, 'UNPAID'),
(133, 2, 58, 3134.67, 1062.35, 1036.23, 26.12, 2098.44, 'UNPAID'),
(134, 2, 59, 2098.44, 1062.35, 1044.87, 17.49, 1053.57, 'UNPAID'),
(135, 2, 60, 1053.57, 1062.35, 1053.57, 8.78, 0.00, 'UNPAID');

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

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `currentKycId`, `nom`, `prenom`, `email`, `numTel`, `role`, `password`, `kycStatus`, `createdAt`, `status`, `created_at`) VALUES
(15, 100, 'Hmani', 'Ines', 'ines.hmani@example.com', '0612340001', 'CLIENT', 'pass1', 'VALIDATED', '2026-02-06 15:17:34', 'REFUSE', '2026-02-16 18:31:17'),
(23, 0, 'John', 'Doe', 'john.doe@gmail.com', '12332112', 'ADMIN', 'password123', 'PENDING', '2026-02-15 20:33:15', 'EN_ATTENTE', '2026-02-16 18:31:17'),
(24, NULL, 'Client', 'Attente', 'client_attente@pidev.local', NULL, 'CLIENT', '$2a$12$1cKSKc/Q/dcB8zIifmIw1uA8JZENc/SOSdKYwF7nsy0pPYiRr0ybq', NULL, '2026-02-16 18:31:46', 'ACCEPTE', '2026-02-16 18:31:46'),
(25, NULL, 'Client', 'Accepte', 'client_accepte@pidev.local', NULL, 'CLIENT', '$2a$12$ZH6yOUqhJjrTUPj3kH520.MnAMMNBFnJEk9uPAyMLsvkIUkp5cvS.', NULL, '2026-02-16 18:31:47', 'REFUSE', '2026-02-16 18:31:47'),
(26, NULL, 'Client32', 'Refuse', 'client_refuse@pidev.local', '87654312', 'CLIENT', '$2a$12$aRWAVJidcrLipCHA/J45mO.kxW3H3SzQwkPEdOZjouKCbefY7Ssii', NULL, '2026-02-16 18:31:47', 'REFUSE', '2026-02-16 18:31:47'),
(27, NULL, 'Admin', 'System', 'admin', NULL, 'ADMIN', 'admin', NULL, '2026-02-16 18:45:15', 'ACCEPTE', '2026-02-16 18:45:15'),
(29, NULL, 'chedi', '', 'chedi@chedi', NULL, 'ADMIN', '$2a$12$.bHeIlewP3GD0CazXU/xB.R5vRSpxUw4tKyylYHjaCmdBBur.vZeq', NULL, '2026-02-16 18:48:21', 'ACCEPTE', '2026-02-16 18:48:21'),
(30, NULL, 'Rana', 'ben atig', 'rana@rana.com', '20557655', 'CLIENT', '$2a$12$KNBMTaWRe3KcflTVnmd2peZ3bBJRVdtEIsuZyC9PNUtfk9j/bgSAu', NULL, '2026-02-16 18:53:47', 'ACCEPTE', '2026-02-16 18:53:47'),
(32, NULL, 'nawel', '', 'nawel@gmail.com', '', 'CLIENT', '$2a$12$4qc5ZAqFQXaqFh.u8W2QZ.s8OUmMWBynBc.kZLGAC1dQXCQd15ghe', NULL, '2026-02-17 08:58:05', 'ACCEPTE', '2026-02-17 08:58:05'),
(46, NULL, 'nidhal', 'azerty', 'nidhal@nidhal.com', '12332145', 'CLIENT', '$2a$12$HMcLlrnK9AT2ZXEsBrkh8OggdNBXsoE3tA38iqCBsTjeT/OwCYMCy', NULL, '2026-02-23 22:49:55', 'EN_ATTENTE', '2026-02-23 22:49:55'),
(54, NULL, 'Said', 'Mariem', 'maryemsaid.42@gmail.com', '12345678', 'CLIENT', '$2a$12$vWXjvYqCTqcjCFB14eahY.9QxJl32abzsOR6fx1stV7sOwXjqWEBa', NULL, '2026-02-24 09:20:55', 'ACCEPTE', '2026-02-24 09:20:55'),
(55, NULL, 'Guedria', 'khaled', 'khaled.guedria@esprit.tn', '87654321', 'CLIENT', '$2a$12$G4PFfMQGF6oiFsEMPGSW1unaomEBySY4Q0h6t7Tlqx/vZuKv5JnI.', NULL, '2026-02-24 09:51:38', 'EN_ATTENTE', '2026-02-24 09:51:38'),
(62, NULL, 'hajji', 'mourad', 'hajji.mourad29@gmail.com', '+21629141000', 'CLIENT', '$2a$12$cAIJQ8H2J90T2GIw1jxzWOs9CvJV4mM6Yf2rYSBV0MkiEu.WkSCKK', NULL, '2026-02-28 14:51:48', 'ACCEPTE', '2026-02-28 14:51:48'),
(63, NULL, 'hajji', 'feryel', 'hajji.feryel@esprit.tn', '+21625456230', 'CLIENT', '$2a$12$Joci2A0Bs4e8FwoRlLTituLF02WnGELwL3MwCpCy5BGBbE/h3X//m', NULL, '2026-02-28 18:09:04', 'ACCEPTE', '2026-02-28 18:09:04'),
(65, NULL, 'hedi', 'kk', 'mohamedhedi322@gmail.com', '58259032', 'CLIENT', '$2a$12$mIxWLMECD7Do52m1AwyEZe3DVYlKsGQ16m19xhZGSw4eBdFEjnzkO', NULL, '2026-03-01 15:49:34', 'ACCEPTE', '2026-03-01 15:49:34'),
(66, NULL, 'hedi', 'kaouech', 'mohamedhedi3222@gmail.com', '56994806', 'CLIENT', 'hedi1234', NULL, '2026-03-01 16:31:20', 'EN_ATTENTE', '2026-03-01 16:31:20');

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

--
-- Dumping data for table `user_badges`
--

INSERT INTO `user_badges` (`id`, `user_id`, `badge_code`, `badge_label`, `awarded_at`) VALUES
(1, 30, 'GUARDIAN', 'Guardian', '2026-02-23 21:33:53'),
(2, 30, 'EARLY_ADOPTER', 'Early Adopter', '2026-02-23 21:33:53'),
(57, 30, 'DAILY_PLAYER', 'Daily Player', '2026-02-23 23:52:31'),
(168, 54, 'SECURITY_CHAMPION', 'Security Champion', '2026-02-24 09:24:54'),
(172, 54, 'GUARDIAN', 'Guardian', '2026-02-24 09:36:45'),
(183, 55, 'SECURITY_CHAMPION', 'Security Champion', '2026-02-24 09:54:23'),
(560, 62, 'GUARDIAN', 'Guardian', '2026-02-28 17:51:48'),
(585, 62, 'EARLY_ADOPTER', 'Early Adopter', '2026-02-28 18:06:44'),
(591, 63, 'GUARDIAN', 'Guardian', '2026-02-28 18:11:00'),
(632, 63, 'EARLY_ADOPTER', 'Early Adopter', '2026-02-28 20:50:24'),
(667, 64, 'GUARDIAN', 'Guardian', '2026-03-01 15:41:26'),
(827, 65, 'GUARDIAN', 'Guardian', '2026-03-01 15:50:26'),
(987, 66, 'GUARDIAN', 'Guardian', '2026-03-01 16:31:58'),
(1022, 65, 'EARLY_ADOPTER', 'Early Adopter', '2026-03-02 00:51:09'),
(1333, 65, 'SECURITY_CHAMPION', 'Security Champion', '2026-03-02 11:09:36');

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

--
-- Dumping data for table `user_gamification`
--

INSERT INTO `user_gamification` (`id`, `user_id`, `points_total`, `level`, `badges`, `last_daily_game_at`, `updated_at`) VALUES
(1, 30, 5, 'STARTER', 'Daily Player', '2026-02-23', '2026-02-23 22:48:14');

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

--
-- Dumping data for table `user_login_audit`
--

INSERT INTO `user_login_audit` (`id`, `user_id`, `email`, `success`, `reason`, `created_at`) VALUES
(1, 27, 'admin', 1, 'ok', '2026-02-23 13:54:13'),
(2, 27, 'admin', 1, 'ok', '2026-02-23 13:58:31'),
(3, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 13:58:56'),
(4, 27, 'admin', 1, 'ok', '2026-02-23 14:00:10'),
(5, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 14:04:47'),
(6, 27, 'admin', 1, 'ok', '2026-02-23 14:04:55'),
(7, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 14:12:31'),
(8, 27, 'admin', 1, 'ok', '2026-02-23 14:14:49'),
(9, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 14:18:47'),
(10, 27, 'admin', 1, 'ok', '2026-02-23 14:19:51'),
(11, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 14:20:33'),
(12, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 14:22:18'),
(13, 27, 'admin', 1, 'ok', '2026-02-23 14:23:18'),
(15, 27, 'admin', 1, 'ok', '2026-02-23 14:26:12'),
(16, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 14:26:44'),
(17, 27, 'admin', 1, 'ok', '2026-02-23 14:27:00'),
(18, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 14:27:29'),
(19, 27, 'admin', 1, 'ok', '2026-02-23 14:37:42'),
(20, 27, 'admin', 1, 'ok', '2026-02-23 14:42:18'),
(21, 27, 'admin', 1, 'ok', '2026-02-23 14:46:09'),
(22, 27, 'admin', 1, 'ok', '2026-02-23 14:51:19'),
(23, 27, 'admin', 1, 'ok', '2026-02-23 15:02:10'),
(24, 27, 'admin', 1, 'ok', '2026-02-23 15:07:39'),
(25, 27, 'admin', 1, 'ok', '2026-02-23 15:10:59'),
(26, 27, 'admin', 1, 'ok', '2026-02-23 15:15:18'),
(27, 27, 'admin', 1, 'ok', '2026-02-23 15:19:45'),
(28, 27, 'admin', 1, 'ok', '2026-02-23 15:25:51'),
(29, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 15:26:20'),
(30, 27, 'admin', 1, 'ok', '2026-02-23 15:30:47'),
(31, 27, 'admin', 1, 'ok', '2026-02-23 15:33:54'),
(32, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 15:43:01'),
(33, 27, 'admin', 1, 'ok', '2026-02-23 15:43:14'),
(34, 27, 'admin', 1, 'ok', '2026-02-23 15:47:42'),
(35, 27, 'admin', 1, 'ok', '2026-02-23 15:48:43'),
(36, 27, 'admin', 1, 'ok', '2026-02-23 15:56:48'),
(37, 27, 'admin', 1, 'ok', '2026-02-23 15:59:01'),
(38, 27, 'admin', 1, 'ok', '2026-02-23 16:00:53'),
(39, 27, 'admin', 1, 'ok', '2026-02-23 20:28:57'),
(40, 27, 'admin', 1, 'ok', '2026-02-23 20:42:37'),
(41, 27, 'admin', 1, 'ok', '2026-02-23 20:52:17'),
(42, 27, 'admin', 1, 'ok', '2026-02-23 21:19:39'),
(43, 27, 'admin', 1, 'ok', '2026-02-23 21:33:44'),
(44, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 21:34:55'),
(45, 27, 'admin', 1, 'ok', '2026-02-23 21:45:39'),
(46, 27, 'admin', 1, 'ok', '2026-02-23 21:54:39'),
(47, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 21:55:07'),
(48, 27, 'admin', 1, 'ok', '2026-02-23 21:56:24'),
(49, 27, 'admin', 1, 'ok', '2026-02-23 21:56:28'),
(50, 27, 'admin', 1, 'ok', '2026-02-23 21:56:31'),
(51, 27, 'admin', 1, 'ok', '2026-02-23 21:56:51'),
(52, 27, 'admin', 1, 'ok', '2026-02-23 21:56:54'),
(53, 27, 'admin', 1, 'ok', '2026-02-23 21:56:57'),
(54, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 21:57:24'),
(55, 27, 'admin', 1, 'ok', '2026-02-23 21:58:45'),
(56, 27, 'admin', 1, 'ok', '2026-02-23 21:59:41'),
(57, 27, 'admin', 1, 'ok', '2026-02-23 22:05:48'),
(58, 27, 'admin', 1, 'ok', '2026-02-23 22:10:32'),
(59, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 22:12:19'),
(60, 27, 'admin', 1, 'ok', '2026-02-23 22:16:26'),
(61, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 22:18:42'),
(62, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 22:23:21'),
(63, 27, 'admin', 1, 'ok', '2026-02-23 22:38:01'),
(65, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 22:47:52'),
(66, 27, 'admin', 1, 'ok', '2026-02-23 22:48:29'),
(67, 27, 'admin', 1, 'ok', '2026-02-23 23:04:58'),
(68, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 23:08:23'),
(70, 27, 'admin', 1, 'ok', '2026-02-23 23:14:39'),
(72, 27, 'admin', 1, 'ok', '2026-02-23 23:23:46'),
(74, 27, 'admin', 1, 'ok', '2026-02-23 23:37:27'),
(76, 27, 'admin', 1, 'ok', '2026-02-23 23:43:17'),
(77, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 23:44:12'),
(78, 27, 'admin', 1, 'ok', '2026-02-23 23:46:12'),
(79, 27, 'admin', 1, 'ok', '2026-02-23 23:48:52'),
(80, 27, 'admin', 1, 'ok', '2026-02-23 23:52:01'),
(81, 30, 'rana@rana.com', 1, 'ok', '2026-02-23 23:52:15'),
(82, 27, 'admin', 1, 'ok', '2026-02-23 23:58:21'),
(83, 27, 'admin', 1, 'ok', '2026-02-24 00:08:54'),
(84, 27, 'admin', 1, 'ok', '2026-02-24 00:21:16'),
(85, 27, 'admin', 1, 'ok', '2026-02-24 00:33:28'),
(86, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 00:34:23'),
(87, 27, 'admin', 1, 'ok', '2026-02-24 01:25:13'),
(90, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 01:27:45'),
(91, 27, 'admin', 1, 'ok', '2026-02-24 01:29:38'),
(92, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 01:35:37'),
(93, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 01:50:06'),
(94, 27, 'admin', 1, 'ok', '2026-02-24 01:50:48'),
(96, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 01:51:18'),
(97, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 01:53:15'),
(98, 27, 'admin', 1, 'ok', '2026-02-24 01:57:59'),
(99, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 02:00:08'),
(100, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 02:05:28'),
(101, 27, 'admin', 1, 'ok', '2026-02-24 02:10:11'),
(102, 27, 'admin', 1, 'ok', '2026-02-24 02:14:03'),
(106, 27, 'admin', 1, 'ok', '2026-02-24 02:27:52'),
(108, 27, 'admin', 1, 'ok', '2026-02-24 02:37:57'),
(109, 27, 'admin', 1, 'ok', '2026-02-24 02:42:36'),
(110, 27, 'admin', 1, 'ok', '2026-02-24 09:07:13'),
(111, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 09:08:05'),
(115, 27, 'admin', 1, 'ok', '2026-02-24 09:09:40'),
(116, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 09:10:40'),
(117, 27, 'admin', 1, 'ok', '2026-02-24 09:11:30'),
(118, 27, 'admin', 1, 'ok', '2026-02-24 09:15:58'),
(120, 27, 'admin', 1, 'ok', '2026-02-24 09:22:35'),
(125, 30, 'rana@rana.com', 1, 'ok', '2026-02-24 09:24:54'),
(133, 27, 'admin', 1, 'ok', '2026-02-24 09:36:14'),
(134, 54, 'maryemsaid.42@gmail.com', 1, 'ok', '2026-02-24 09:36:45'),
(135, 27, 'admin', 1, 'ok', '2026-02-24 09:46:55'),
(137, 27, 'admin', 1, 'ok', '2026-02-24 09:53:08'),
(142, 54, 'maryemsaid.42@gmail.com', 1, 'ok', '2026-02-24 09:54:23'),
(143, 27, 'admin', 1, 'ok', '2026-02-27 22:06:27'),
(145, 30, 'rana@rana.com', 1, 'ok', '2026-02-27 22:07:25'),
(146, 30, 'rana@rana.com', 1, 'ok', '2026-02-27 22:18:15'),
(147, 30, 'rana@rana.com', 1, 'ok', '2026-02-27 22:40:27'),
(148, 27, 'admin', 1, 'ok', '2026-02-27 22:45:09'),
(149, 27, 'admin', 1, 'ok', '2026-02-27 22:45:49'),
(150, 30, 'rana@rana.com', 1, 'ok', '2026-02-27 22:49:59'),
(151, 27, 'admin', 1, 'ok', '2026-02-27 22:53:11'),
(152, 27, 'admin', 1, 'ok', '2026-02-27 23:01:35'),
(153, 27, 'admin', 1, 'ok', '2026-02-27 23:01:41'),
(154, 27, 'admin', 1, 'ok', '2026-02-27 23:08:14'),
(155, 27, 'admin', 1, 'ok', '2026-02-27 23:13:17'),
(156, 27, 'admin', 1, 'ok', '2026-02-27 23:21:54'),
(157, 27, 'admin', 1, 'ok', '2026-02-27 23:35:18'),
(158, 27, 'admin', 1, 'ok', '2026-02-27 23:41:30'),
(159, 27, 'admin', 1, 'ok', '2026-02-27 23:51:43'),
(160, 27, 'admin', 1, 'ok', '2026-02-27 23:58:17'),
(161, 27, 'admin', 1, 'ok', '2026-02-28 00:02:38'),
(162, 27, 'admin', 1, 'ok', '2026-02-28 00:14:04'),
(164, 30, 'rana@rana.com', 1, 'ok', '2026-02-28 00:15:47'),
(165, 27, 'admin', 1, 'ok', '2026-02-28 00:18:36'),
(166, 27, 'admin', 1, 'ok', '2026-02-28 00:20:24'),
(167, 27, 'admin', 1, 'ok', '2026-02-28 00:27:37'),
(168, 27, 'admin', 1, 'ok', '2026-02-28 00:32:16'),
(169, 27, 'admin', 1, 'ok', '2026-02-28 00:35:04'),
(170, 27, 'admin', 1, 'ok', '2026-02-28 00:39:41'),
(172, 27, 'admin', 1, 'ok', '2026-02-28 00:50:02'),
(174, 27, 'admin', 1, 'ok', '2026-02-28 00:53:37'),
(182, 27, 'admin', 1, 'ok', '2026-02-28 01:42:13'),
(183, 27, 'admin', 1, 'ok', '2026-02-28 01:44:07'),
(186, 62, 'hajji.mourad29@gmail.com', 1, 'ok', '2026-02-28 17:51:48'),
(187, 27, 'admin', 1, 'ok', '2026-02-28 17:53:32'),
(188, 62, 'hajji.mourad29@gmail.com', 1, 'ok', '2026-02-28 17:54:03'),
(189, 62, 'hajji.mourad29@gmail.com', 1, 'ok', '2026-02-28 17:59:06'),
(190, 62, 'hajji.mourad29@gmail.com', 1, 'ok', '2026-02-28 18:01:50'),
(191, 62, 'hajji.mourad29@gmail.com', 1, 'ok', '2026-02-28 18:06:44'),
(192, NULL, 'hajji.feryel@esprit.tn', 0, 'Votre compte est en attente de validation.', '2026-02-28 18:09:31'),
(193, 27, 'admin', 1, 'ok', '2026-02-28 18:10:21'),
(194, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-02-28 18:11:00'),
(195, 27, 'admin', 1, 'ok', '2026-02-28 18:11:50'),
(196, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-02-28 18:12:13'),
(197, 62, 'hajji.mourad29@gmail.com', 1, 'ok', '2026-02-28 20:02:01'),
(198, 27, 'admin', 1, 'ok', '2026-02-28 20:02:42'),
(199, 27, 'admin', 1, 'ok', '2026-02-28 20:06:20'),
(200, 27, 'admin', 1, 'ok', '2026-02-28 20:08:58'),
(201, 27, 'admin', 1, 'ok', '2026-02-28 20:11:46'),
(202, 27, 'admin', 1, 'ok', '2026-02-28 20:15:00'),
(203, 27, 'admin', 1, 'ok', '2026-02-28 20:17:53'),
(204, 27, 'admin', 1, 'ok', '2026-02-28 20:21:11'),
(205, 27, 'admin', 1, 'ok', '2026-02-28 20:22:16'),
(206, NULL, 'hajji.feryel@esprit.tn', 0, 'Email ou mot de passe invalide.', '2026-02-28 20:33:14'),
(207, NULL, 'hajji.feryel@esprit.tn', 0, 'Email ou mot de passe invalide.', '2026-02-28 20:33:49'),
(208, NULL, 'hajji.feryel@esprit.tn', 0, 'Email ou mot de passe invalide.', '2026-02-28 20:33:55'),
(209, NULL, 'hajji.feryel@esprit.tn', 0, 'Email ou mot de passe invalide.', '2026-02-28 20:34:08'),
(210, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-02-28 20:35:43'),
(211, 27, 'admin', 1, 'ok', '2026-02-28 20:42:04'),
(212, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-02-28 20:43:39'),
(213, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-02-28 20:50:23'),
(214, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-02-28 20:56:25'),
(215, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-02-28 21:01:11'),
(216, 27, 'admin', 1, 'ok', '2026-03-01 15:36:46'),
(217, 64, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-01 15:41:26'),
(218, 64, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-01 15:44:35'),
(219, 64, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-01 15:47:24'),
(220, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-01 15:50:26'),
(221, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-01 15:56:19'),
(222, 27, 'admin', 1, 'ok', '2026-03-01 15:56:57'),
(223, 27, 'admin', 1, 'ok', '2026-03-01 16:02:55'),
(224, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-01 16:11:41'),
(225, 27, 'admin', 1, 'ok', '2026-03-01 16:12:39'),
(226, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-01 16:29:16'),
(227, 66, 'mohamedhedi3222@gmail.com', 1, 'ok', '2026-03-01 16:31:58'),
(228, 27, 'admin', 1, 'ok', '2026-03-02 00:44:54'),
(229, NULL, 'mohamedhedi322@gmail.com', 0, 'Email ou mot de passe invalide.', '2026-03-02 00:50:40'),
(230, NULL, 'mohamedhedi322@gmail.com', 0, 'Email ou mot de passe invalide.', '2026-03-02 00:50:54'),
(231, NULL, 'mohamedhedi322@gmail.com', 0, 'Email ou mot de passe invalide.', '2026-03-02 00:50:56'),
(232, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 00:51:09'),
(233, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 00:54:42'),
(234, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 00:59:25'),
(235, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 10:22:39'),
(236, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 10:30:37'),
(237, 27, 'admin', 1, 'ok', '2026-03-02 10:32:36'),
(238, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 10:33:22'),
(239, 27, 'admin', 1, 'ok', '2026-03-02 10:34:54'),
(240, 27, 'admin', 1, 'ok', '2026-03-02 10:42:09'),
(241, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 10:45:07'),
(242, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 10:50:43'),
(243, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 10:51:52'),
(244, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 10:52:55'),
(245, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 10:54:18'),
(246, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 10:57:39'),
(247, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 11:00:14'),
(248, NULL, 'mohamedhedi322@gmail.com', 0, 'Email ou mot de passe invalide.', '2026-03-02 11:22:24'),
(249, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 11:22:31'),
(250, NULL, 'mohamedhedi322@gmail.com', 0, 'Email ou mot de passe invalide.', '2026-03-02 11:28:27'),
(251, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 11:28:34'),
(252, 27, 'admin', 1, 'ok', '2026-03-02 11:32:08'),
(253, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 11:34:38'),
(254, NULL, 'mohamedhedi3222@gmail.com', 0, 'Email ou mot de passe invalide.', '2026-03-02 11:36:52'),
(255, NULL, 'mohamedhedi3222@gmail.com', 0, 'Email ou mot de passe invalide.', '2026-03-02 11:36:58'),
(256, 66, 'mohamedhedi3222@gmail.com', 1, 'ok', '2026-03-02 11:37:28'),
(257, 27, 'admin', 1, 'ok', '2026-03-02 11:41:11'),
(258, 27, 'admin', 1, 'ok', '2026-03-02 11:41:27'),
(259, 27, 'admin', 1, 'ok', '2026-03-02 11:56:40'),
(260, 27, 'admin', 1, 'ok', '2026-03-02 12:08:16'),
(261, 27, 'admin', 1, 'ok', '2026-03-02 12:40:01'),
(262, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 12:40:47'),
(263, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 16:14:20'),
(264, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 16:22:47'),
(265, 27, 'admin', 1, 'ok', '2026-03-02 16:23:55'),
(266, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 16:27:08'),
(267, 27, 'admin', 1, 'ok', '2026-03-02 16:28:13'),
(268, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 16:30:27'),
(269, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 17:06:44'),
(270, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 17:11:13'),
(271, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 17:16:44'),
(272, 27, 'admin', 1, 'ok', '2026-03-02 17:17:26'),
(273, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 22:36:40'),
(274, 27, 'admin', 1, 'ok', '2026-03-02 22:39:47'),
(275, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-02 22:45:40'),
(276, 27, 'admin', 1, 'ok', '2026-03-02 22:52:08'),
(277, NULL, 'mohamedhedi322@gmail.com', 0, 'Email ou mot de passe invalide.', '2026-03-03 01:01:51'),
(278, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-03 01:01:56'),
(279, 27, 'admin', 1, 'ok', '2026-03-03 01:03:12'),
(280, NULL, 'hajji.feryelesprit.tn', 0, 'Email ou mot de passe invalide.', '2026-03-03 08:41:19'),
(281, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-03-03 08:41:30'),
(282, 27, 'admin', 1, 'ok', '2026-03-03 08:41:56'),
(283, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-03-03 08:42:30'),
(284, 27, 'admin', 1, 'ok', '2026-03-03 08:45:28'),
(285, NULL, 'ines.hmani@example.com', 0, 'Votre compte a ete refuse.', '2026-03-03 08:48:38'),
(286, NULL, 'john.doe@gmail.com', 0, 'Votre compte est en attente de validation.', '2026-03-03 08:49:20'),
(287, 27, 'admin', 1, 'ok', '2026-03-03 08:50:04'),
(288, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-03 08:52:51'),
(289, 27, 'admin', 1, 'ok', '2026-03-03 09:01:48'),
(290, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-03-03 09:06:35'),
(291, 27, 'admin', 1, 'ok', '2026-03-03 09:11:38'),
(292, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-03-03 09:17:21'),
(293, 27, 'admin', 1, 'ok', '2026-03-03 09:26:59'),
(294, 63, 'hajji.feryel@esprit.tn', 1, 'ok', '2026-03-03 09:30:07'),
(295, 27, 'admin', 1, 'ok', '2026-03-03 09:35:12'),
(296, 27, 'admin', 1, 'ok', '2026-03-03 09:40:36'),
(297, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-03 09:42:20'),
(298, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-04 12:28:41'),
(299, 65, 'mohamedhedi322@gmail.com', 1, 'ok', '2026-03-04 12:39:28'),
(300, 27, 'admin', 1, 'ok', '2026-03-04 12:40:15'),
(301, 27, 'admin', 1, 'ok', '2026-03-04 12:43:58'),
(302, 27, 'admin', 1, 'ok', '2026-03-04 12:47:25');

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

--
-- Dumping data for table `user_security_challenges`
--

INSERT INTO `user_security_challenges` (`id`, `user_id`, `challenge_code`, `challenge_title`, `status`, `progress`, `target`, `updated_at`) VALUES
(10, 32, 'CH_PROFILE_COMPLETE', 'Completer profil', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(11, 32, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(12, 32, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(13, 30, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(14, 30, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(15, 30, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(16, 25, 'CH_PROFILE_COMPLETE', 'Completer profil', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(17, 25, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(18, 25, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(19, 26, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(20, 26, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(21, 26, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(22, 24, 'CH_PROFILE_COMPLETE', 'Completer profil', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(23, 24, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(24, 24, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(25, 15, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-04 12:47:28'),
(26, 15, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-04 12:47:28'),
(27, 15, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'PENDING', 0, 1, '2026-03-04 12:47:28'),
(31, 23, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-02-28 21:01:23'),
(32, 23, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-02-28 21:01:23'),
(33, 23, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'PENDING', 0, 1, '2026-02-28 21:01:23'),
(496, 46, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(497, 46, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(498, 46, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(2716, 54, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(2717, 54, 'CH_ENABLE_MFA', 'Activer MFA', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(2718, 54, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(2848, 55, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(2849, 55, 'CH_ENABLE_MFA', 'Activer MFA', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(2850, 55, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(5008, 62, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(5009, 62, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(5010, 62, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(5203, 63, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(5204, 63, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(5205, 63, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(5527, 64, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-01 15:47:36'),
(5528, 64, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-01 15:47:36'),
(5529, 64, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'DONE', 1, 1, '2026-03-01 15:47:36'),
(6103, 65, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(6104, 65, 'CH_ENABLE_MFA', 'Activer MFA', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(6105, 65, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(6679, 66, 'CH_PROFILE_COMPLETE', 'Completer profil', 'DONE', 1, 1, '2026-03-04 12:47:27'),
(6680, 66, 'CH_ENABLE_MFA', 'Activer MFA', 'PENDING', 0, 1, '2026-03-04 12:47:27'),
(6681, 66, 'CH_STRONG_PASSWORD', 'Connexion securisee sans risque', 'DONE', 1, 1, '2026-03-04 12:47:27');

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
-- Dumping data for table `wallet`
--

INSERT INTO `wallet` (`id_wallet`, `id_user`, `nom_proprietaire`, `telephone`, `email`, `code_acces`, `est_actif`, `solde`, `plafond_decouvert`, `devise`, `statut`, `date_creation`, `tentatives_echouees`, `date_derniere_tentative`, `est_bloque`) VALUES
(141, 65, 'kk hedi', '58259032', 'mohamedhedi322@gmail.com', '431739', 1, 498344.2, 0, 'TND', 'ACTIVE', '2026-03-04 12:29:26', 0, NULL, 0);

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
-- Indexes for table `ah`
--
ALTER TABLE `ah`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `alerte`
--
ALTER TABLE `alerte`
  ADD PRIMARY KEY (`idAlerte`),
  ADD KEY `idCategorie` (`idCategorie`);

--
-- Indexes for table `author`
--
ALTER TABLE `author`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `book`
--
ALTER TABLE `book`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_CBE5A331F675F31B` (`author_id`);

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
-- Indexes for table `faq`
--
ALTER TABLE `faq`
  ADD PRIMARY KEY (`id`);

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `admin_task_history`
--
ALTER TABLE `admin_task_history`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `ah`
--
ALTER TABLE `ah`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `alerte`
--
ALTER TABLE `alerte`
  MODIFY `idAlerte` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT for table `author`
--
ALTER TABLE `author`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1237;

--
-- AUTO_INCREMENT for table `book`
--
ALTER TABLE `book`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `categorie`
--
ALTER TABLE `categorie`
  MODIFY `idCategorie` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `chat_messages`
--
ALTER TABLE `chat_messages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=41;

--
-- AUTO_INCREMENT for table `cheque`
--
ALTER TABLE `cheque`
  MODIFY `id_cheque` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `clients`
--
ALTER TABLE `clients`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `faq`
--
ALTER TABLE `faq`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `feedback`
--
ALTER TABLE `feedback`
  MODIFY `id_feedback` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=190;

--
-- AUTO_INCREMENT for table `gamification_events`
--
ALTER TABLE `gamification_events`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5386;

--
-- AUTO_INCREMENT for table `historique_scores`
--
ALTER TABLE `historique_scores`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `item`
--
ALTER TABLE `item`
  MODIFY `idItem` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT for table `kyc`
--
ALTER TABLE `kyc`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `kyc_files`
--
ALTER TABLE `kyc_files`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `loan`
--
ALTER TABLE `loan`
  MODIFY `loanId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT for table `otp_audit`
--
ALTER TABLE `otp_audit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `password_reset`
--
ALTER TABLE `password_reset`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `product`
--
ALTER TABLE `product`
  MODIFY `productId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `productsubscription`
--
ALTER TABLE `productsubscription`
  MODIFY `subscriptionId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT for table `publication`
--
ALTER TABLE `publication`
  MODIFY `id_publication` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- AUTO_INCREMENT for table `repayment`
--
ALTER TABLE `repayment`
  MODIFY `repayId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=137;

--
-- AUTO_INCREMENT for table `security_events`
--
ALTER TABLE `security_events`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `transaction`
--
ALTER TABLE `transaction`
  MODIFY `id_transaction` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=156;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=67;

--
-- AUTO_INCREMENT for table `user_badges`
--
ALTER TABLE `user_badges`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2723;

--
-- AUTO_INCREMENT for table `user_gamification`
--
ALTER TABLE `user_gamification`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `user_login_audit`
--
ALTER TABLE `user_login_audit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=303;

--
-- AUTO_INCREMENT for table `user_qr_tokens`
--
ALTER TABLE `user_qr_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user_security_challenges`
--
ALTER TABLE `user_security_challenges`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11983;

--
-- AUTO_INCREMENT for table `wallet`
--
ALTER TABLE `wallet`
  MODIFY `id_wallet` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=142;

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
-- Constraints for table `book`
--
ALTER TABLE `book`
  ADD CONSTRAINT `FK_CBE5A331F675F31B` FOREIGN KEY (`author_id`) REFERENCES `author` (`id`) ON DELETE CASCADE;

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
