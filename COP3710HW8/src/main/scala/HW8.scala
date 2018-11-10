import org.apache.spark._
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.rdd.RDD

object HW8 {
  def main(args: Array[String]) {
    // setting hadoop directory
    //System.setProperty("hadoop.home.dir", "c:\\hadoop")

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // connecting to spark driver
    val conf = new SparkConf().setAppName("Hw8").setMaster("local")
    val spark = new SparkContext(conf)

    // load the file
    val lines = spark.textFile("basketball_words_only.txt").persist()

    taskOne(lines)
    println()
    taskTwo(lines)
  }

  def taskOne(lines: RDD[String]): Unit = {
    val words = lines.flatMap(line => line.split(" ")).persist()
    val totalWords = words.count()

    val counts = words.map(word => (word, 1))
                      .reduceByKey((x,y) => x+y)
                      .sortBy(_._2, ascending = false)
    var wordCounts = counts.collect()
    var output = "Words that account for at least 3% are "

    for (pair <- wordCounts) {
      if (pair._2 > totalWords * 0.03) {
        output = output.concat("\"" + pair._1 + "\", ")
      }
    }
    println(output)
    println()

    for (i <- 0 to 3) {
      printf("%s appears %d times%n", wordCounts(i)._1, wordCounts(i)._2)
    }
  }

  def taskTwo(lines: RDD[String]): Unit = {
    val wordL = lines.flatMap(line => line.split(" ").dropRight(1))
    val wordR = lines.flatMap(line => line.split(" ").drop(1))

    val pairWord = wordL zip wordR
    val counts = pairWord.map(pair => (pair, 1))
                         .reduceByKey((x,y) => x+y)
                         .sortBy(_._2, ascending = false)
                         .map(triple => (triple._1._1, (triple._1._2, triple._2)))
                         .persist()
    val basketball = counts.lookup("basketball").head
    val the = counts.lookup("the").head
    val competitive = counts.lookup("competitive").head

    printf("\"basketball\" is followed by \"%s\" %d times.%n", basketball._1,
      basketball._2)
    printf("\"the\" is followed by \"%s\" %d times.%n", the._1, the._2)
    printf("\"competitive\" is followed by \"%s\" %d times.%n", competitive._1,
      competitive._2)

  }
}