package com.inokey.solution.dnk.nucleus.enum
enum class MultiplannerProject {
    PLANILOISIR, PLANICOURSE, PLANIVENTE, PLANIDECOUVERTE

    //Future projects can be added here
    ,
    PLANIHEALTHCARE, PLANIFOOD, PLANILEARN, PLANIHOME, PLANIZEN, PLANIEVENT, PLANILOVE;
}

/** Description métier courte par projet (sans modifier l’enum existant). */
fun MultiplannerProject.description(): String = when (this) {
    MultiplannerProject.PLANILOISIR ->
        "Planifier des séjours/activités (solo, couple, famille, amis) avec IA + géoloc. Programmes, budget, logements."

    MultiplannerProject.PLANICOURSE ->
        "Préparer et optimiser les courses : listes intelligentes, comparateur de prix, itinéraires multi-magasins."

    MultiplannerProject.PLANIVENTE ->
        "Aider les commerces locaux à vendre : catalogue, promos, commandes, statistiques, multi-succursales."

    MultiplannerProject.PLANIDECOUVERTE ->
        "Découvrir autour de soi en contexte (météo, humeur, temps dispo). Idées immédiates, cartes, favoris."

    MultiplannerProject.PLANIHEALTHCARE ->
        "Santé & bien-être : suivi d’habitudes (sommeil, hydratation), rappels, conseils et rendez-vous."

    MultiplannerProject.PLANIFOOD ->
        "Nutrition & cuisine : menus personnalisés, recettes, panier auto lié aux courses, contraintes alimentaires."

    MultiplannerProject.PLANILEARN ->
        "Micro-apprentissage : parcours, quiz IA, révisions espacées, objectifs et progression."

    MultiplannerProject.PLANIHOME ->
        "Maison & déco : moodboards, estimation des coûts, liste de matériaux, liens marchands."

    MultiplannerProject.PLANIZEN ->
        "Bien-être mental : audios guidés, exercices courts, routines anti-stress, suivi d’humeur."

    MultiplannerProject.PLANIEVENT ->
        "Événements : planification, invitations, to-do, budget, intégration billetterie locale."

    MultiplannerProject.PLANILOVE ->
        "Planification de rendez-vous romantiques : idées personnalisées, lieux, activités et surprises."
}

/** Métadonnées utiles pour ‘recréer’/scaffolder un projet côté doc ou UI. */
data class ProjectDescriptor(
    val code: MultiplannerProject,
    val title: String,
    val summary: String,
    val typicalUsers: List<String>,
    val keyFeatures: List<String>,
    val notificationTemplates: Map<NotificationType, String>
)

fun MultiplannerProject.descriptor(): ProjectDescriptor {
    val title = when (this) {
        MultiplannerProject.PLANILOISIR -> "PlaniLoisir"
        MultiplannerProject.PLANICOURSE -> "PlaniCourse"
        MultiplannerProject.PLANIVENTE -> "PlaniVente"
        MultiplannerProject.PLANIDECOUVERTE -> "PlaniDécouverte"
        MultiplannerProject.PLANIHEALTHCARE -> "PlaniHealthCare"
        MultiplannerProject.PLANIFOOD -> "PlaniFood"
        MultiplannerProject.PLANILEARN -> "PlaniLearn"
        MultiplannerProject.PLANIHOME -> "PlaniHome"
        MultiplannerProject.PLANIZEN -> "PlaniZen"
        MultiplannerProject.PLANIEVENT -> "PlaniEvent"
        MultiplannerProject.PLANILOVE -> "PlaniLove"

    }

    val (users, features) = when (this) {
        MultiplannerProject.PLANILOISIR ->
            listOf("Individus", "Couples", "Familles", "Amis") to
                    listOf("Suggestions IA", "Programme/journées", "Budget", "Hébergements", "Itinéraires")

        MultiplannerProject.PLANICOURSE ->
            listOf("B2C", "Associations", "Petits pros") to
                    listOf("Listes intelligentes", "Comparaison de prix", "Itinéraires multi-magasins", "Historique")

        MultiplannerProject.PLANIVENTE ->
            listOf("Commerçants", "Gérants", "Réseaux/Franchises") to
                    listOf("Catalogue/Promos", "Commandes", "Stats", "Multi-succursales")

        MultiplannerProject.PLANIDECOUVERTE ->
            listOf("Grand public") to
                    listOf("Idées instantanées", "Cartes & filtres", "Favoris")

        MultiplannerProject.PLANIHEALTHCARE ->
            listOf("Grand public", "Familles") to
                    listOf("Suivi habitudes", "Rappels", "Conseils")

        MultiplannerProject.PLANIFOOD ->
            listOf("Familles", "Sportifs", "Allergies") to
                    listOf("Menus personnalisés", "Recettes", "Panier auto", "Contraintes")

        MultiplannerProject.PLANILEARN ->
            listOf("Étudiants", "Pros") to
                    listOf("Parcours", "Quiz IA", "Révisions espacées", "Objectifs")

        MultiplannerProject.PLANIHOME ->
            listOf("Particuliers") to
                    listOf("Moodboards", "Estimation coûts", "Matériaux", "Liens marchands")

        MultiplannerProject.PLANIZEN ->
            listOf("Grand public") to
                    listOf("Exercices courts", "Audios guidés", "Routines anti-stress", "Suivi d’humeur")

        MultiplannerProject.PLANIEVENT ->
            listOf("Particuliers", "Assos", "Commerces") to
                    listOf("Planif. événements", "Invitations/RSVP", "Check-list", "Budget")

        MultiplannerProject.PLANILOVE ->
            listOf("Couples", "Individus") to
                    listOf("Idées personnalisées", "Lieux romantiques", "Activités", "Surprises")
    }

    // ⚠️ On ne touche pas à tes méthodes existantes : on génère des templates “safe” ici
    val templates = mapOf(
        NotificationType.REGISTRATION to this.safeTemplateFor(NotificationType.REGISTRATION),
        NotificationType.REMINDER to this.safeTemplateFor(NotificationType.REMINDER),
        NotificationType.INVITE to this.safeTemplateFor(NotificationType.INVITE)
    )

    return ProjectDescriptor(
        code = this,
        title = title,
        summary = this.description(),
        typicalUsers = users,
        keyFeatures = features,
        notificationTemplates = templates
    )
}

/** Helpers ‘safe’ pour les nouveaux projets — sans modifier tes méthodes historiques. */
fun MultiplannerProject.safeDefaultNotificationTemplate(): String = when (this) {
    MultiplannerProject.PLANILOISIR -> "notification-planiloisir-fr.html"
    MultiplannerProject.PLANICOURSE -> "notification-planicourse-fr.html"
    MultiplannerProject.PLANIVENTE -> "notification-planivente-fr.html"
    MultiplannerProject.PLANIDECOUVERTE -> "notification-planidecouverte-fr.html"
    else -> "notification-${name.lowercase()}-fr.html"
}

fun MultiplannerProject.safeTemplateFor(type: NotificationType): String {
    val slug = name.lowercase()
    return when (type) {
        NotificationType.REGISTRATION -> when (this) {
            MultiplannerProject.PLANILOISIR -> "registration-planiloisir-fr.html"
            MultiplannerProject.PLANICOURSE -> "registration-planicourse-fr.html"
            MultiplannerProject.PLANIVENTE -> "registration-planivente-fr.html"
            MultiplannerProject.PLANIDECOUVERTE -> "registration-planidecouverte-fr.html"
            else -> "registration-$slug-fr.html"
        }

        NotificationType.REMINDER -> when (this) {
            MultiplannerProject.PLANILOISIR -> "reminder-planiloisir-fr.html"
            MultiplannerProject.PLANICOURSE -> "reminder-planicourse-fr.html"
            MultiplannerProject.PLANIVENTE -> "reminder-planivente-fr.html"
            MultiplannerProject.PLANIDECOUVERTE -> "reminder-planidecouverte-fr.html"
            else -> "reminder-$slug-fr.html"
        }

        NotificationType.INVITE -> when (this) {
            MultiplannerProject.PLANILOISIR -> "invite-planiloisir-fr.html"
            MultiplannerProject.PLANICOURSE -> "invite-planicourse-fr.html"
            MultiplannerProject.PLANIVENTE -> "invite-planivente-fr.html"
            MultiplannerProject.PLANIDECOUVERTE -> "invite-planidecouverte-fr.html"
            else -> "invite-$slug-fr.html"
        }
    }
}

enum class NotificationType { REGISTRATION, REMINDER, INVITE }