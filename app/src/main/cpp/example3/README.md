
[[转]显示列表，VA，VBO，VAO，EBO的区别](https://www.cnblogs.com/BigFeng/p/5117311.html)
====================================================================================

~~1.glVertex~~
--------------

最原始的设置顶点方法，在glBegin和glEnd之间使用。OpenGL3.0已经废弃此方法。
每个glVertex与GPU进行一次通信，十分低效。

```oclight
glBegin(GL_TRIANGLES);
    glVertex(0, 0);
    glVertex(1, 1);
    glVertex(2, 2);
glEnd();
```

~~2.显示列表(glCallList)~~
-------------------------

每个glVertex调用都与GPU进行一次通信，显示列表是收集好所有的顶点，一次性的发送给GPU。
缺点是在绘制之前就要把要传给GPU的顶点准备好，传后就不能修改了。

```oclight
GLuint glassList;
glNewList(glassList, GL_COMPILE);
    DrawGlass();
glEndList();

glCallList(glassList);
```

~~3.顶点数组(Vertex Array)~~
---------------------------

顶点数组也是收集好所有的顶点，一次性发送给GPU。不过数据不是存储于GPU中的，绘制速度上没有显示列表快，
优点是可以修改数据。

```oclight
#define MEDIUM_STARS 40
M3DVector2f vMediumStars[MEDIUM_STARS];
//在这做点vMediumStars的设置//
glVertexPointer(2, GL_FLOAT, 0, vMediumStars);
glDrawArrays(GL_POINTS, 0, MEDIUM_STARS);
```


**显示列表和顶点数组都是过时的东西了，下面的VBO和VAO才是重点！**


4.VBO(Vertex Buffer Object)顶点缓冲区对象
---------------------------------------

VBO，全称为Vertex Buffer Object，与FBO，PBO并称，但它实际上老不少。就某种意义来说，
他就是VA(Vertex Array)的升级版。VBO出现的背景是人们发现VA和显示列表还有让人不满足的地方。
一般，在OpenGL里，提高顶点绘制的办法：

(1)显示列表：把常规的glBegin()-glEnd()中的代码放到一个显示列表中(通常在初始化阶段完成)，
然后每遍渲染都调用这个显示列表。

(2)VA：使用顶点数组，把顶点以及顶点属性数据作为数组，渲染的时候直接用一个或几个函数调动这些数组里的数据进行绘制，
形式上是减少函数调用的次数(告别glVertex)，提高绘制效率。

但是，这两种方法都有缺点。VA是在客户端设置的，所以执行这类函数(glDrawArray或glDrawElement)后，
客户端还得把得到的顶点数据向服务端传输一次(所谓的“二次处理”)，这样一来就有了不必要的动作了，
降低了效率——如果我们写的函数能直接把顶点数据发送给服务端就好了——这正是VBO的特性之一。显示列表的缺点在于它的古板，
一旦设定就不容许修改，所以它只适合对一些“固定”的东西的绘制进行包装。
(我们无办法直接在硬件层改顶点数据，因为这是脱离了流水线的事物)。
而VBO直接把顶点数据交到流水线的第一步，与显示列表的效率还是有差距，但它这样就得到了操作数据的弹性——渲染阶段，
我们的VBO绘制函数持续把顶点数据交给流水线，在某一刻我们可以把该帧到达了流水线的顶点数据取回客户端修改(Vertex mapping)，
再提交回流水线(Vertex unmapping)，或者用glBufferData或glBufferSubData重新全部或buffer提交修改了的顶点数据，
这是VBO的另一个特性。

VBO结合了VA和显示列表这个说法不太妥当，应该说它结合了两者的一些特性，绘制效率在两者之间，
且拥有良好的数据更改弹性。这种折衷造就了它一直为目前最高的地位。

通过顶点缓冲对象(Vertex Buffer Objects, VBO)管理这个内存，它会在GPU内存（通常被称为显存）中储存大量顶点。
使用这些缓冲对象的好处是我们可以一次性的发送一大批数据到显卡上，而不是每个顶点发送一次。

```oclight
//创建VBO及VBO赋值
glGenBuffers(1, &m_nPositionVBO);
glBufferData(GL_ARRAY_BUFFER, sizeof(posData), posData, GL_STREAM_DRAW);

glGenBuffers(1, &m_nTexcoordVBO);
glBufferData(GL_ARRAY_BUFFER, sizeof(texData), texData, GL_STREAM_DRAW);

glGenBuffers(1, &m_nIndexVBO);
glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indexData), indexData, GL_STATIC_DRAW);

//代码一，不使用shader VBO已经创建好了
glBindBuffer(GL_ARRAY_BUFFER, m_nPositionVBO);
glEnableClientState(GL_VERTEX_ARRAY);
glVertexPointer(2, GL_FLOAT, 0, NULL);

glBindBuffer(GL_ARRAY_BUFFER, m_nTexcoordVBO);
glEnableClientState(GL_TEXTURE_COORD_ARRAY);
glTexCoordPointer(2, GL_FLOAT, 0, NULL);

glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m_nIndexVBO);
glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, NULL);
glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, NULL);

glDisableClientState(GL_TEXTURE_COORD_ARRAY);
glDisableClientState(GL_VERTEX_ARRAY);

glBindBuffer(GL_ARRAY_BUFFER, NULL);

//代码二，使用shader
glBindBuffer(GL_ARRAY_BUFFER, m_nPositionVBO);
glEnableVertexAttribArray(VAT_POSITION);
glVertexAttribPointer(VAT_POSITION, 2, GL_INT, GL_FALSE, 0, NULL);

glBindBuffer(GL_ARRAY_BUFFER, m_nTexcoordVBO);
glEnableVertexAttribArray(VAT_TEXCOORD);
glVertexAttribPointer(VAT_TEXCOORD, 2, GL_INT, GL_FALSE, 0, NULL);

glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m_nIndexVBO);
glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, NULL);

glDisableVertexAttribArray(VAT_POSITION);
glDisableVertexAttribArray(VAT_TEXCOORD);

glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, NULL);
glBindBuffer(GL_ARRAY_BUFFER, NULL);
```

5.VAO(Vertex Array Object)顶点数组对象
-------------------------------------

VBO将顶点信息放到GPU中，GPU在渲染时去缓存中取数据，二者中间的桥梁是GL-Context。GL-Context整个程序一般只有一个，
所以如果一个渲染流程里有两份不同的绘制代码，GL-context就负责在他们之间进行切换。这也是为什么要在渲染过程中，
在每份绘制代码之中会有glBindbuffer、glEnableVertexAttribArray、glVertexAttribPointer。那么优化的方法来了，
把这些都放到初始化时候完成吧！
*VAO记录该次绘制所需要的所有VBO所需信息，把它保存到VBO特定位置，绘制的时候直接在这个位置取信息绘制。*

VAO的全名是Vertex Array Object，首先，它不是Buffer-Object，所以不用作存储数据；
其次，它针对“顶点”而言，也就是说它跟“顶点的绘制”息息相关。（VAO和VA没有任何关系）

VAO可以像顶点缓冲对象(VBO)那样被绑定，任何随后的顶点属性调用都会储存在这个VAO中。
这样的好处就是，当配置顶点属性指针时，你只需要将那些调用执行一次，之后再绘制物体的时候只需要绑定相应的VAO就行了。
这使在不同顶点数据和属性配置之间切换变得非常简单，只需要绑定不同的VAO就行了。

OpenGL的核心模式(Core-profile)要求我们使用VAO，所以它知道该如何处理我们的顶点输入。
如果我们绑定VAO失败，OpenGL会拒绝绘制任何东西。

```oclight
glGenBuffers(1, &m_nQuadPositionVBO);  
glBindBuffer(GL_ARRAY_BUFFER, m_nQuadPositionVBO);  
glBufferData(GL_ARRAY_BUFFER, sizeof(fQuadPos), fQuadPos, GL_STREAM_DRAW);  

glGenBuffers(1, &m_nQuadTexcoordVBO);  
glBindBuffer(GL_ARRAY_BUFFER, m_nQuadTexcoordVBO);  
glBufferData(GL_ARRAY_BUFFER, sizeof(fQuadTexcoord), fQuadTexcoord, GL_STREAM_DRAW);  

glGenBuffers(1, &m_nQuadIndexVBO);  
glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m_nQuadIndexVBO);  
glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(nQuadIndex), nQuadIndex, GL_STREAM_DRAW);  

//VAO 初始化部分
glGenVertexArrays(1, &m_nQuadVAO);
glBindVertexArray(m_nQuadVAO);

//开始保存状态
glBindBuffer(GL_ARRAY_BUFFER, m_nQuadPositionVBO);  
glEnableVertexAttribArray(VAT_POSITION);  
glVertexAttribPointer(VAT_POSITION, 2, GL_INT, GL_FALSE, 0, NULL);

glBindBuffer(GL_ARRAY_BUFFER, m_nQuadTexcoordVBO);  
glEnableVertexAttribArray(VAT_TEXCOORD);  
glVertexAttribPointer(VAT_TEXCOORD, 2, GL_INT, GL_FALSE, 0, NULL);  

glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m_nQuadIndexVBO);   
//保存结束
glBindVertexArray(NULL);

glBindBuffer(GL_ARRAY_BUFFER, NULL);
glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, NULL);
```

6.EBO(Element Buffer Object)索引缓冲对象
---------------------------------------

在渲染顶点这一话题上我们还有最有一个需要讨论的东西——索引缓冲对象(Element Buffer Object，EBO，
也叫Index Buffer Object，IBO)。和顶点缓冲对象一样，EBO也是一个缓冲，它专门储存索引，
OpenGL调用这些顶点的索引来决定该绘制哪个顶点。

![各缓冲对象关系](vertex_array_objects_ebo.png)
