# GreenDaoDemo
一、背景知识
ORM(Object Relation Mapping):对象关系模型。用于实现面向对象编程语言里不同类型系统的数据之间的转换。从效果上说，它其实是创建了一个可在编程语言里使用的“虚拟对象数据库”。ORM作为项目中间件形式实现数据在不同场景下数据关系映射，对象关系映射是一种为了解决面向对象与关系数据库存在的互不匹配的现象的技术。

Android常用几个ORM框架：ORMLite、GreenDao、ormndroid、androrm等。ORMLite和GreenDao的比较：

ORMLite：文档全面，社区活跃，维护良好，使用简单，容易上手，但是因为基于反射，所效率较低
GreenDao：使用code generation,效率很高；库文件较小(约87KB)，占用更少内存, 缺点是学习成本较高，需要弄清楚其原理才能方便使用
二、原理简介
GreenDao向SQLite数据库提供了一个对象导向的接口，它为用户省下了很多重复的工作，而且提供了简便的操作接口。
为了使用GreenDao，需要在新建一个Java工程（工程中需要导入greendao-generator-x.x.x.jar，freemarker-x.x.xx.jar），根据GreenDao的规则在其中描述数据库的表结构，运行之后它会构建你的实体模型和DAO工具类。具体包括：

DaoMaster:

持有数据库对象(SQLiteDatabase) ，并管理一些DAO类(不是对象)
能够创建和删除数据库表
它的内部类OpenHelper和DevOpenHelper是SQLiteOpenHelper的实现类，用于创建SQLite数据库的模式
DaoSession:

管理制定模式下所有可用的DAO对象
能对实体进行插入、加载、更新、刷新、删除操作。
DAO:

每个实体都有一个DAO，相对于DaoSession,它有更多的方法，比如：加载全部、InsertTx
Entity

可持久化的对象，由generator 生成。相当于数据库中的一张表，所有字段都是使用标准的Java对象的属性(比诶)
通过generator生成的这些工具类，你就可以在自己的Android工程中对进行数据库操作，完全不需要写任何SQL语句。

三、建表
数据库建立：在应用中通过DaoMaster的DevOpenHelper完成。

Schema schema = new Schema(1000, "de.greenrobot.daoexample");
Entity note = schema.addEntity("Note");
        note.addIdProperty().primaryKey().autoincrement();
        note.addStringProperty("text").notNull();
        note.addStringProperty("comment");
        note.addDateProperty("date");
注：在生成的实体类中,int类型会自动转为long类型

dao.setTableName("NoteList");
greenDAO会自动根据实体类属性创建表字段，并赋予默认值。例如在数据库方面的表名和列名都来源于实体类名和属性名。默认的数据库名称是大写使用下划线分隔单词，而不是在Java中使用的驼峰式大小写风格。例如，一个名为“CREATIONDATE”属性将成为一个数据库列“CREATION_DATE”。

在使用greenDAO时，一个实体类只能对应一个表，目前没法做到一个表对应多个实体类，或者多个表共用一种对象类型。后续的升级也不会针对这一点进行扩展。

四、表的增删改查
1.查询：

(1)QueryBuilder：
普通用法：

List joes = userDao.queryBuilder()
.where(Properties.FirstName.eq("Joe"))
.orderAsc(Properties.LastName)
.list();
嵌套情况：(查询1970年9月之后出生的用户)

QueryBuilder qb = userDao.queryBuilder();
qb.where(Properties.FirstName.eq("Joe"),
qb.or(Properties.YearOfBirth.gt(1970),
qb.and(Properties.YearOfBirth.eq(1970), Properties.MonthOfBirth.ge(10))));
List youngJoes = qb.list();
(2)Query：
可多次执行的查询，使用QueryBuilder实际上调用了Query类，若果相同的查询要执行多次的话，那应该调用QueryBuilder的build()方法来创建Query，而不是直接使用Query。

只返回一个结果：调用Query或者QueryBuilder的unique()方法，如果不希望返回null，那么可调用uniqueOrThrow()方法。
返回多个结果：

list() 所有实体被加载到内存中，结果通常是一个ArrayList
listLazy() 实体在需要时才载入内存。表中的一个元素被第一次访问时会被缓存，下次再访问时使用缓存
listLazyUncached() 任何对列表元素的访问都会导致从数据库中加载
listIterator() 以按需加载的方式来遍历结果，数据没有被缓存。
后三种方式使用了LazyList类，持有了一个数据库cursor来实现按需加载。这样是为了确保关闭LazyList和iterators。
一旦所有元素都被访问或遍历过，来自listLazy()的cache lazy list和来自listIterator()方法的lazy iterator将会自动关闭cursor。
然而，如果list...的处理过早的完成了，你应该调用 close()手动关闭。

(3)提高多次查询效率
一旦使用QueryBuilder创建了一个query，那么这个Query对象就可以就可以被复用来执行查询显然这种方式逼重新创建一次Query效率要高。
具体来说：

如果Query的参数没有变更，你只需要再次调用List/unuque方法即可
如果参数发生了变化，那么就需要通过setParameter方法来处理每一个发生改变的参数。
举例：

Query query = userDao.queryBuilder().where(Properties.FirstName.eq("Joe"), Properties.YearOfBirth.eq(1970)).build();
List joesOf1970 = query.list();
现在复用该Query对象：

query.setParameter(0, "Maria");
query.setParameter(1, 1977);
List mariasOf1977 = query.list();
由此可见，Query在执行一次build之后会将查询结果进行缓存，方便下次继续使用。

(4)原生SQL语句
两种方法：

Query query = userDao.queryBuilder().where(
new StringCondition("_ID IN " +
"(SELECT USER_ID FROM USER_MESSAGE WHERE READ_FLAG = 0)").build();
如果这里的QueryBuilder没有提供你想要的特性，可以使用原始的queryRaw或queryRawCreate方法。

Query query = userDao.queryRawCreate(  ", GROUP G WHERE G.NAME=? AND T.GROUP_ID=G._ID", "admin");
注：写SQL语句时推荐定义常量来表示表名或者表项，这样可以防止出错，因为编译器会检查。

(5)删除
一次性删除多个指定条件的数据：调用QueryBuilder的buildDelete方法，它会返回一个DeleteQuery。
如果数据被缓存过，你可以激活那些将要被删除的实体。如果导致了一些使用的问题。你可以考虑清除identity scope。

(6)调试
如果你的query没有返回期望的结果，这里有两个静态的flag，可以开启QueryBuilder身上的SQL和参数的log。

QueryBuilder.LOG_SQL = true;
QueryBuilder.LOG_VALUES = true;
它们会在任何build方法调用的时候打印出SQL命令和传入的值。这样你可以对你的期望值进行对比，或许也能够帮助你复制SQL语句到某些
SQLite 数据库的查看器中，执行并获取结果，以便进行比较。

(7)其它
统计查询：queryBuilder.buildCount().count();
获取全部记录：noteDao.loadAll();

2.插入、更新
普通方法：

Note note = new Note(null, noteText, comment, new Date());
noteDao.insert(note);
更新、批量插入：

photoDao.insertOrReplace(photo);
photoDao.insertInTx(photo);
关于数据操作的性能问题：(摘自官网)
Inserting/updating/deleting entities runs very slow. What’s wrong?
Probably, you are inserting/updating/deleting entities without using a transaction. Thus each operation is considered a transaction and SQLite needs to write to disk and do a file sync for each operation. Running all operations in a single transaction is essential for performance (and usually makes sense in terms of consistency). It will run a magnitude faster. For example, running 1,000 inserts in a single transaction ran 500 times faster (!) than 1000 individual transactions in one of our performance tests.

In greenDAO, use DaoSession.runInTx(Runnable) to make the given Runnable run as a transaction. If you have a list of entities of the same type, you can use the insertInTx or updateInTx methods of the DAO class belonging to the entity.


性能对比：
普通用法：400条插入，大约5秒

long startTime = System.currentTimeMillis();
for (int i = 0; i != 400; i++) {
    Person person = new Person(null,i+"");
    personDao.insert(person);
}
long endTime = System.currentTimeMillis();
Log.e("result",""+(endTime - startTime));
开多线程：0.1s以内

daoSession.runInTx(new Runnable() {
    @Override
    public void run() {
    long startTime = System.currentTimeMillis();
        for (int i = 0; i != 400; i++) {
            Person person = new Person(null, i + "");
            personDao.insert(person);
        }
        long endTime = System.currentTimeMillis();
            Log.e("result", "" + (endTime - startTime));
        }
     }
);
批量插入：0.1s以内

long startTime = System.currentTimeMillis();
Person[] persons = new Person[400];
for (int i = 0; i != 400; i++) {
    persons[i] = new Person(null, i + "");
}
personDao.insertInTx(persons);
long endTime = System.currentTimeMillis();
Log.e("result", "" + (endTime - startTime));
3.删除
清空数据表：

noteDao.deleteAll()
删除些数据：

QueryBuilder<CityInfo> qb = cityInfoDao.queryBuilder();
DeleteQuery<CityInfo> bd = qb.where(Properties.Id.eq(Id)).buildDelete();
bd.executeDeleteWithoutDetachingEntities();
Deletes all matching entities without detaching them from the identity scope

public void deleteAllMessages() {
 
        RcMessagesDao messageDao = daoSession.getRcMessagesDao();
 
 
        QueryBuilder qb = messageDao.queryBuilder();
 
        qb.where(ca.acesoft.reatchat.RcMessagesDao.Properties.Favorite.eq(false));
 
        List messageReceived = qb.list();
 
        messageDao.deleteInTx(messageReceived);
 
    }