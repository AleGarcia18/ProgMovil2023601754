import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    while (true) {
        println("\nMenú:")
        println("1. Sumar tres números")
        println("2. Ingresar nombre completo")
        println("3. Calcular tiempo vivido")
        println("4. Salir")
        print("Seleccione una opción: ")

        when (scanner.nextInt()) {
            1 -> sumarNumeros(scanner)
            2 -> ingresarNombre(scanner)
            3 -> calcularTiempoVivido(scanner)
            4 -> salirPrograma()
            else -> println("Opción inválida. Intente de nuevo.")
        }
    }
}

fun sumarNumeros(scanner: Scanner) {
    print("Ingrese el primer número: ")
    val num1 = scanner.nextInt()
    print("Ingrese el segundo número: ")
    val num2 = scanner.nextInt()
    print("Ingrese el tercer número: ")
    val num3 = scanner.nextInt()
    val suma = num1 + num2 + num3
    println("La suma es: $suma")
}

fun ingresarNombre(scanner: Scanner) {
    scanner.nextLine() // Consumir el salto de línea
    print("Ingrese su nombre completo: ")
    val nombre = scanner.nextLine()
    println("Hola, $nombre!")
}

fun calcularTiempoVivido(scanner: Scanner) {
    print("Ingrese su año de nacimiento (YYYY): ")
    val year = scanner.nextInt()
    print("Ingrese su mes de nacimiento (MM): ")
    val month = scanner.nextInt()
    print("Ingrese su día de nacimiento (DD): ")
    val day = scanner.nextInt()

    val fechaNacimiento = LocalDate.of(year, month, day)
    val fechaActual = LocalDate.now()
    val periodo = Period.between(fechaNacimiento, fechaActual)
    val diasTotales = ChronoUnit.DAYS.between(fechaNacimiento, fechaActual)
    val horas = diasTotales * 24
    val minutos = horas * 60
    val segundos = minutos * 60

    println("Has vivido:")
    println("- ${periodo.years} años, ${periodo.months} meses y ${periodo.days} días")
    println("- Total en meses: ${periodo.years * 12 + periodo.months}")
    println("- Total en semanas: ${diasTotales / 7}")
    println("- Total en días: $diasTotales")
    println("- Total en horas: $horas")
    println("- Total en minutos: $minutos")
    println("- Total en segundos: $segundos")
}

fun salirPrograma() {
    println("Programa finalizado.")
    System.exit(0)
}
