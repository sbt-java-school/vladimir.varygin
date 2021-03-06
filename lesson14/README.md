# Задание №14

## Реализовать ThreadPool

## Реализация

Класс [ThreadPool](https://github.com/sbt-java-school/vladimir.varygin/tree/master/lesson14/src/main/java/lesson14/home/ThreadPool.java) предназначен для 
распределённого вычисления пула задач. Класс предоставляет следующие возможности:

1. Решение конечной очереди задач и завершение всех потоков под них (задачи) выделенных
2. Решение бесконечной очереди задач и зачершении всех потоков через заданное время после отсутствия поступления новых задач
3. Решение бесконечной очереди задач без завершения потоков

Для корректной работы программы все задачи в очереди должны реализовывать интерфейс Task.

Для добавления новых задач в очередь предусмотренны два метода:

1. addTask(Task task) - добавление одной задачи в очередь
2. addTask(Queue<Task> tasks) - добавление очереди задач

Для контроля конечной очереди на пустоту для завершения всех потоков используется дополнительный поток Killer.
Все потоки реализуются в классе Worker.
Синхронизация для потоков Worker осущствляется по объекту очереди, а для потока Killer по специальному объекту killerWatcher.

## Полученные навыки

Реализация распределённого вычисления пула задач. Навык синхронизации доступа к общему ресурсу
потоков и нотификации ожидающих потоков при помощи функций wait и notify.
Навык прерывания потоков при помощи метода interrupt.