package org.example;

import org.example.Model.User.Kyc;
import org.example.Model.User.User;
import org.example.Service.UserService.KycService;
import org.example.Service.UserService.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        UserService userService = new UserService();
        KycService kycService = new KycService();

        // ================== 1️⃣ Inscription ==================
        User user = new User(
                0,                  // currentKycId
                "John",             // nom
                "Doe",              // prenom
                "john.doe@example.com", // email
                "0600000000",       // numTel
                "USER",             // role
                "password123",      // password
                "PENDING",          // kycStatus
                LocalDateTime.now() // createdAt
        );

        if (userService.Add(user)) {
            System.out.println("Utilisateur créé, id=" + user.getId() + ", KYC non rempli.");
        } else {
            System.out.println("Erreur création utilisateur ou email déjà existant.");
            return;
        }

        // ================== 2️⃣ Remplissage du KYC ==================
        // Simuler des fichiers binaires
        byte[] fakeDocumentFront = new byte[10];
        Arrays.fill(fakeDocumentFront, (byte) 1);
        byte[] fakeDocumentBack = new byte[10];
        Arrays.fill(fakeDocumentBack, (byte) 2);
        byte[] fakeSignature = new byte[10];
        Arrays.fill(fakeSignature, (byte) 3);
        byte[] fakeSelfie = new byte[10];
        Arrays.fill(fakeSelfie, (byte) 4);

        // Hachage simple du numéro de carte (pour test)
        String cardNumber = "1234567812345678";
        String cardHash = Integer.toString(cardNumber.hashCode());
        String last4Digits = cardNumber.substring(cardNumber.length() - 4);

        // Créer le KYC
        Kyc kyc = new Kyc(
                user.getId(),
                "CARTE_ID",
                cardHash,
                last4Digits,
                fakeDocumentFront,
                fakeDocumentBack,
                fakeSignature,
                fakeSelfie,
                "PENDING",
                LocalDateTime.now()
        );
    }}
