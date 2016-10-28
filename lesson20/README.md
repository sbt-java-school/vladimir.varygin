# Задание по лекции №20

Разработать класс(классы) по конвертации произвольных объектов классов: 
Integer, Long, Float, Double, BigDecimal, String в любой требуемый объект: 
Integer, Long, Float, Double, BigDecimal, String.

Реализация должна быть написана таким образом, чтобы была возможность легко 
добавить новые правила конвертации любого произвольного объекта в любой другой произвольный объект. 

Должны быть тесты.

## Реализация

Поставленную задачу реализуют два интерфейса: **Converter** и **ConverterValue**.
При инициализации класса, реализующего интерфейс Converter, происходит добавление
в мапу конвертеров по умолчанию, в которые можно конвертировать объекты. При инициализации
этих конвертеров происходит автоматическое добавление всех возможных конвертеров из 
каких стандартных объектов можно конвертировать в инициализируемом конвертере.

К примеру, мы хотим конвертировать в Integer из String. Для этого необходимо
добавить конвертер для типа Integer (если он ещё не определён) при помощи метода **addConverterTo**, 
а так же конвертер для типа Integer из String при помощи метода **addConverterFrom**.

Если необходимо полностью изменить конвертацию для типа Integer, то можно удалить
стандартный конвертер при помощи метода **removeConverterTo** или же удалить
конкретный конвертер из некоторого типа при помощи метода **removeConverterFrom**.

Можно написать полностью новый конвертер из некоторого типа в некоторый при помощи
интерфейса ConverterValue. Для этого необходимо создать свою реализацию интерфейса
ConverterValue с родительским классом AbstractConverterValue для типа Т. Эта реализация
должна переопределять метод **initDefaultConverters**, заполняющий мапу конвертерами, 
и метод **convert**, вызывающий соответствующие конвертеры из мапы. Далее необходимо
создать собственно сами конвертеры, реализующие интерфейс ConverterValue и переопределяющие
всего один метод convert, где должна происходить конечная конвертация из известного 
типа в необходимый.

Если нет необходимости полностью переопределять некоторый тип, 
то достаточно добавить/переопределить конечный конвертер при 
помощи функции addConverterFrom для существующего результирующего типа.

## Тесты

Для проверки конвертера написаны тесты по конвертации перечисленных типов.
Кроме того, написан тест на несуществующий тип конвертера и тест на добавление
нового конвертера (из экземпляра класса Person в String).

Запуск всех тестов осуществляется в классе **AllTests**.